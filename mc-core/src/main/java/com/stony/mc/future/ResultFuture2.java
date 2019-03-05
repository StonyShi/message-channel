package com.stony.mc.future;

import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午3:07
 * @since 2019/1/4
 * @see ResultFuture
 */
@Deprecated
public class ResultFuture2<V> implements Future<V> {

    final ExchangeProtocol request;
    final long id;
    final long createTimeMs;
    long updateTimeMs;
    volatile V value;
    volatile boolean done = false;
    volatile boolean cancelled = false;
    ChannelHandlerContext ctx;
    final Lock lock;
    final Condition doneCondition;

    public static ResultFuture2 wrap(ExchangeProtocol msg) {
        return new ResultFuture2(msg);
    }
    public ResultFuture2(ExchangeProtocol msg) {
        this.request = msg;
        this.id = msg.getId();
        this.lock = new ReentrantLock();
        this.doneCondition = lock.newCondition();
        this.createTimeMs = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            lock.lock();
            this.cancelled = true;
            this.updateTimeMs = System.currentTimeMillis();
            doneCondition.signalAll();
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            lock.lock();
            if(isDone() || isCancelled()) {
                return value;
            }
            doneCondition.await();
            return value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            lock.lock();
            if(isDone() || isCancelled()) {
                return value;
            }
            doneCondition.await(timeout, unit);
            return value;
        } finally {
            lock.unlock();
        }
    }
    public void set(V v){
        try {
            lock.lock();
            this.value = v;
            this.done = true;
            this.updateTimeMs = System.currentTimeMillis();
            doneCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void setContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean isSuccess() {
        return done && !cancelled;
    }

    public long getId() {
        return id;
    }

    public ExchangeProtocol getRequest() {
        return request;
    }
}