package com.stony.mc.metrics;

import java.time.Duration;

/**
 * <p>mc-core
 * <p>com.stony.mc.metrics
 *
 * @author stony
 * @version 下午2:35
 * @since 2019/1/14
 */
public class EmptyMetricEventListener implements MetricEventListener {
    @Override
    public void onEvent(MetricEvent event, Duration duration, Long value) {

    }
}
