package com.stony.mc;

import java.util.concurrent.TimeUnit;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午9:59
 * @since 2019/1/3
 */
public class EventsSubject<E> {

    int NO_DURATION = -1;
    Object NO_VALUE = null;
    Throwable NO_ERROR = null;
    TimeUnit NO_TIME_UNIT = null;

    public void onEvent(E event, long duration, TimeUnit timeUnit, Throwable throwable, Object value) {
        System.out.println(String.format("event:%s, duration:%s 豪秒, value:%s",
                event, timeUnit.toMillis(duration), value));
    }

    public void onEvent(E event, long durationInMillis, Throwable throwable, Object value) {
        onEvent(event, durationInMillis, TimeUnit.MILLISECONDS, throwable, value);
    }

    public void onEvent(E event) {
        onEvent(event, NO_DURATION, NO_TIME_UNIT, NO_ERROR, NO_VALUE);
    }

    public void onEvent(E event, Throwable throwable) {
        onEvent(event, NO_DURATION, NO_TIME_UNIT, throwable, NO_VALUE);
    }

    public void onEvent(E event, long duration, TimeUnit timeUnit) {
        onEvent(event, duration, timeUnit, NO_ERROR, NO_VALUE);
    }

    public void onEvent(E event, long duration, TimeUnit timeUnit, Throwable throwable) {
        onEvent(event, duration, timeUnit, throwable, NO_VALUE);
    }

    public void onEvent(E event, long duration, TimeUnit timeUnit, Object value) {
        onEvent(event, duration, timeUnit, NO_ERROR, value);
    }

    public void onEvent(E event, long durationInMillis) {
        onEvent(event, durationInMillis, TimeUnit.MILLISECONDS, NO_ERROR, NO_VALUE);
    }

    public void onEvent(E event, long durationInMillis, Throwable throwable) {
        onEvent(event, durationInMillis, TimeUnit.MILLISECONDS, throwable, NO_VALUE);
    }

    public void onEvent(E event, long durationInMillis, Object value) {
        onEvent(event, durationInMillis, TimeUnit.MILLISECONDS, NO_ERROR, value);
    }

    public void onEvent(E event, Object value) {
        onEvent(event, NO_DURATION, NO_TIME_UNIT, NO_ERROR, value);
    }


}