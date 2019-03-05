package com.stony.mc.ids;


import com.stony.mc.Logging;
import com.stony.mc.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>server
 * <p>com.stony.mc.ids
 *
 * 32 bit 作为秒数
 * 10 bit 作为机器编号 10位的长度最多支持部署1024个节点
 * 6  bit 作为标记位
 * 16 bit 作为秒内序列号 - 16位的计数顺序号支持每个节点每毫秒产生2^16-1个ID序号：65535
 * ----------------------------------------------------------------------------
               32bit                        5bit     5bit   6bit      16bit
     ----------------------------------     -----   -----  -----     ------------
     000000000-000000000-000000000-0... -   00000 - 00000 - 000000 - 000000000000
     ----------------------------------     -----   -----  -----   ------------
             |                                |       |      |       |
             |                                |       |      |       |
      seconds time seq                        |       |      |       |
                                dataCenter    |       |      |       |
                                          workerId   db-id   |       |
                                                           flag-v    |
                                                                  seq-value
 * -----------------------------------------------------------------------------
 * @author stony
 * @version 下午5:19
 * @since 2018/1/12
 */
public class SimpleIdGenerator extends Logging implements IdGenerator {

    //================================================Algorithm's Parameter=============================================
    // 机器id所占的位数 最大值2^5次方减一
    private final long workerIdBits = 5L;

    // 数据中心标识id所占的位数
    private final long dataCenterIdBits = 5L;

    // 标记位所占的位数
    private final long flagIdBits = 6L;

    // 机器码支持的id(十进制)，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    // -1L 左移 5位 (worker id 所占位数) 即 5位二进制所能获得的最大十进制数 31
    // 等同于 (1L << workerIdBits) - 1 或者 -1L ^ (-1L << workerIdBits)
    private final long maxWorkerId = ~(-1L << workerIdBits);

    // 数据中心支持的最大标识id  31
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits);

    // 支持的最大数据标识id  63
    private final long flagIdValueMask = ~(-1L << flagIdBits);

    // 序列在id中占的位数
    private final long sequenceBits = 16L;

    // 生成序列的掩码(16位所对应的最大整数值)，为65535
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 标记flag 左移位数  16 (sequence 所占用的位数)
    private final long flagIdMoveBits = sequenceBits;

    // 机器ID 左移位数  22(16+6) (sequence位数 + 标记位数)
    private final long workerIdMoveBits = sequenceBits + flagIdBits;

    // 数据标识id 左移位数  27(16+6+5) (sequence位数 + 标记位数 + 机器Id位数)
    private final long dataCenterIdMoveBits = sequenceBits + flagIdBits + workerIdBits;

    // 时间截向 左移位数  32(16+6+5+5) (sequence位数 + 标记位数 + 机器Id位数 + 数据中心Id位数)
    private final long timestampMoveBits = sequenceBits + flagIdBits + workerIdBits + dataCenterIdBits;

    //=================================================Works's Parameter================================================
    /**
     * 工作机器ID(0~31)
     */
    private long workerId;
    /**
     * 数据中心ID(0~31)
     */
    private long dataCenterId;
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    //===============================================Constructors=======================================================
    public SimpleIdGenerator(long machineId) {
        long maxMachineId = (maxDataCenterId +1) * (maxWorkerId +1) - 1;
        if (workerId > maxMachineId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Machine Id can't be greater than %d or less than 0", maxMachineId));
        }
        this.dataCenterId = (machineId >> workerIdBits) & maxDataCenterId;
        this.workerId = machineId & maxWorkerId;
    }
    /**
     * 构造函数
     * @param workerId     工作ID     (0~31)
     * @param dataCenterId 数据中心ID (0~31)
     */
    public SimpleIdGenerator(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("DataCenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    @Override
    public synchronized long nextId() {

        long timestamp = currentTime();
        Result result = getSequenceResult(timestamp, lastTimestamp, sequence, sequenceMask);
        timestamp = result.timestamp;
        sequence = result.sequence;
        //更新生成ID的时间截
        long timestampSeconds = timestamp/1000L;
        lastTimestamp = timestamp;
        long flagIdValue = ThreadLocalRandom.current().nextLong(1, flagIdValueMask) & flagIdValueMask;
        if(isDebugEnabled()) {
            debug("[{}]-[{}]-[{}]", format.format(new Date(lastTimestamp)), workerId, sequence);
        }
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestampSeconds) << timestampMoveBits) // 左移时间戳
                | (dataCenterId << dataCenterIdMoveBits)      // 左移数据中心Id
                | (workerId << workerIdMoveBits)              // 左移机器Id
                | (flagIdValue << flagIdMoveBits)              // 左移标记值
                | sequence;
    }

    public static SimpleIdGenerator getInstance() {
        return new SimpleIdGenerator(NetUtils.getWorkId());
    }

}