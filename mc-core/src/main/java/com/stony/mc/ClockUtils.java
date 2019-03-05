package com.stony.mc;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午9:58
 * @since 2019/1/3
 */
public abstract class ClockUtils {
    public static long newStartTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long onEndMillis(long startTimeMillis) {
        return System.currentTimeMillis() - startTimeMillis;
    }
}