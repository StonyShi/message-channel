package com.stony.mc.ids;

import com.stony.mc.Logging;
import com.stony.mc.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>server
 * <p>com.stony.mc.ids
 *
 *  1 bit 最高位0，避免溢出，ID变为负值
 * 41 bit 时间毫秒数 - 41位的长度可以使用69年, 存储时间的差值（当前时间 - 开始时间)
 * 10 bit 机器编号 （5个bit是数据中心 + 5个bit的机器ID） - 10位的长度最多支持部署1024个节点
 * 12 bit 毫秒内序列号 - 12位的计数顺序号支持每个节点每毫秒产生4096-1个ID序号
 * -------------------------------------------------------------------------
               41bit                            5bit    5bit      12bit
     ----------------------------------------   -----   -----   ------------
   0-000000000-000000000-000000000-0000000... - 00000 - 00000 - 000000000000
     ----------------------------------------   -----   -----   -------------
                    |                            |       |          |
                    |                            |       |          |
            milliseconds time seq                |       |          |
                                            worker-id    |          |
                                                       db-id        |
                                                                 seq-value
 * --------------------------------------------------------------------------
 * @author stony
 * @version 下午4:24
 * @since 2018/1/12
 */
public class SnowflakeIdGenerator extends Logging implements IdGenerator{

    //================================================Algorithm's Parameter=============================================
    // 开始时间截(毫秒) 2019-01-01 00:00:00
    private final long startTime = 1546272000000L;

    // 机器id所占的位数 最大值2^5次方减一
    private final long workerIdBits = 5L;

    // 数据标识id所占的位数
    private final long dataCenterIdBits = 5L;

    // 支持的最大机器id(十进制)，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    // -1L 左移 5位 (worker id 所占位数) 即 5位二进制所能获得的最大十进制数 31
    // 等同于 (1L << workerIdBits) - 1 或者 -1L ^ (-1L << workerIdBits)
    private final long maxWorkerId = ~(-1L << workerIdBits);

    // 支持的最大数据标识id  31
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits);

    // 序列在id中占的位数
    private final long sequenceBits = 12L;

    // 生成序列的掩码(12位所对应的最大整数值)，为4095 (0b111111111111=0xfff=4095)
    private final long sequenceMask = ~(-1L << sequenceBits);

    // 机器ID 左移位数  12 (即 sequence 所占用的位数)
    private final long workerIdMoveBits = sequenceBits;

    // 数据标识id 左移位数  17(5+12) (机器Id位数 + sequence位数)
    private final long dataCenterIdMoveBits = sequenceBits + workerIdBits;

    // 时间截向 左移位数  22(5+5+12) (机器Id位数 + 数据中心Id位数 + sequence位数)
    private final long timestampMoveBits = sequenceBits + workerIdBits + dataCenterIdBits;

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
    public SnowflakeIdGenerator(long machineId) {
        long maxMachineId = (maxDataCenterId +1) * (maxWorkerId +1) - 1;
        if (machineId > maxMachineId || machineId < 0) {
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
    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
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
    public synchronized long nextId()  {
        long timestamp = currentTime();
        Result result = getSequenceResult(timestamp, lastTimestamp, sequence, sequenceMask);

        sequence = result.sequence;
        //更新生成ID的时间截
        lastTimestamp = result.timestamp;
        if(isDebugEnabled()) {
            debug("[{}]-[{}]-[{}]", format.format(new Date(lastTimestamp)), workerId, sequence);
        }
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - startTime) << timestampMoveBits) // 左移时间戳
                | (dataCenterId << dataCenterIdMoveBits)      // 左移数据中心Id
                | (workerId << workerIdMoveBits)              // 左移机器Id
                | sequence;
    }

    public static SnowflakeIdGenerator getInstance() {
        return new SnowflakeIdGenerator(NetUtils.getWorkId());
    }
}