package com.stony.mc.manager;

import com.stony.mc.NetUtils;
import com.stony.mc.session.HostPort;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午5:29
 * @since 2019/1/17
 */
public class ChannelContextHolder {
    AtomicInteger subscribeCount = new AtomicInteger();
    final ChannelHandlerContext ctx;
    final HostPort hostPort;
    AtomicBoolean processing = new AtomicBoolean();
    volatile Long lastProcessingTime = System.currentTimeMillis();

    public ChannelContextHolder(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.hostPort = NetUtils.getChannelHostPort(ctx);
    }

    public boolean isProcessing() {
        return processing.get();
    }

    public void beginProcessing() {
        this.lastProcessingTime = System.currentTimeMillis();
        this.processing.compareAndSet(false, true);
    }

    public void endProcessing() {
        this.lastProcessingTime = System.currentTimeMillis();
        this.processing.compareAndSet(true, false);
    }

    public Long getLastProcessingTime() {
        return lastProcessingTime;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void unsubscribe() {
        this.subscribeCount.decrementAndGet();
    }
    public void subscribe() {
        this.subscribeCount.incrementAndGet();
    }
    public boolean isSubscribe() {
        return subscribeCount.get() > 0;
    }

    public HostPort getHostPort() {
        return hostPort;
    }
    public String getAddress() {
        return hostPort.line();
    }

    @Override
    public String toString() {
        return "{" +
                "ctx=" + ctx +
                ", hostPort='" + hostPort.line() + '\'' +
                ", isProcessing=" + processing +
                ", lastProcessingTime=" + lastProcessingTime +
                '}';
    }
}