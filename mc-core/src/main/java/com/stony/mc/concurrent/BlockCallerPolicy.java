package com.stony.mc.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>mc-core
 * <p>com.stony.mc.store
 *
 * @author stony
 * @version 下午2:48
 * @since 2019/1/1
 */
public class BlockCallerPolicy implements RejectedExecutionHandler{
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            throw new RejectedExecutionException(String.format("Task %s rejected from ", r.toString()), e);
        }
    }
}