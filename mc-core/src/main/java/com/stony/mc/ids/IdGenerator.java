package com.stony.mc.ids;

import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>server
 * <p>com.stony.mc.id
 *
 * @author stony
 * @version 下午4:24
 * @since 2018/1/12
 */
public interface IdGenerator {


    long nextId();

    /**
     * 同一时间生成的，则进行序列++
     * @param timestamp
     * @param lastTimestamp
     * @param sequence
     * @param sequenceMask
     * @return
     */
    default Result getSequenceResult(long timestamp, long lastTimestamp, long sequence, long sequenceMask) {
        //时钟回拨之后 时间偏差小于5ms重试
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                //时间偏差大小小于5ms，则等待两倍时间
                try {
                    wait(offset << 1);  //wait
                    timestamp = currentTime();
                } catch (InterruptedException ignore) {}
            }
        }
        //如果当前时间小于上一次ID生成的时间戳: 说明系统时钟回退过 - 这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出 即 序列 > 4095
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = blockTillNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置0~9
        else {
            sequence = ThreadLocalRandom.current().nextLong(10);
        }
        return new Result(timestamp, sequence);
    }

    /**
     * 获得以毫秒为单位的当前时间
     */
    default long currentTime() {
        return System.currentTimeMillis();
    }
    /**
     * 阻塞到下一个毫秒 即 直到获得新的时间戳
     */
    default long blockTillNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    class Result {
        long timestamp;
        long sequence;
        public Result(long timestamp, long sequence) {
            this.timestamp = timestamp;
            this.sequence = sequence;
        }
    }
}
