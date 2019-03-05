package com.stony.mc.concurrent;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>mc-core
 * <p>com.stony.mc.store
 *
 * @author stony
 * @version 下午2:49
 * @since 2019/1/1
 */
public class TaskTransferQueue extends LinkedTransferQueue<Runnable> {
    volatile ThreadPoolExecutor executor;

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public boolean force(Runnable o) {
        if (executor == null || executor.isShutdown())
            throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
        return super.offer(o);
    }

    public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
        if (executor == null || executor.isShutdown())
            throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
        return super.offer(o, timeout, unit);
    }

    @Override
    public boolean offer(Runnable e) {
        // first try to transfer to a waiting worker thread
        if (!tryTransfer(e)) {
            // check if there might be spare capacity in the thread
            // pool elasticExecutor
            int left = executor.getMaximumPoolSize() - executor.getCorePoolSize();
            if (left > 0) {
                // reject queuing the task to force the thread pool
                // elasticExecutor to add a worker if it can; combined
                // with ForceQueuePolicy, this causes the thread
                // pool to always scale up to max pool size and we
                // only queue when there is no spare capacity
                return false;
            } else {
                return super.offer(e);
            }
        } else {
            return true;
        }
    }
}