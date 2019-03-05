package com.stony.mc.metrics;

import java.time.Duration;

/**
 * <p>mc-core
 * <p>com.stony.mc.metrics
 *
 * @author stony
 * @version 上午10:44
 * @since 2019/1/14
 */
public interface MetricEventListener {
    void onEvent(MetricEvent event, Duration duration, Long value);

    default void onEvent(MetricEvent event, Duration duration) {
        onEvent(event, duration, null);
    }

    default void onEvent(MetricEvent event, Long value) {
        onEvent(event, null, value);
    }
    default void onEvent(MetricEvent event) {
        onEvent(event, null, 1L);
    }
}