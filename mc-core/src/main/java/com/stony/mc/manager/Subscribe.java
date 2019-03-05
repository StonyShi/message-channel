package com.stony.mc.manager;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * <p>mc-core
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午3:20
 * @since 2019/1/8
 */
public class Subscribe {
    private static final Logger logger = LoggerFactory.getLogger(Subscribe.class);
    ChannelHandlerContext ctx;
    String group;
    String topic;
    String key;
    final long createTime;
    AtomicLong writeCount = new AtomicLong();
    AtomicLong writeCountSuccess = new AtomicLong();
    AtomicLong writeCountFailed = new AtomicLong();
    public Subscribe(ChannelHandlerContext ctx, String group, String topic, String key) {
        this.ctx = Objects.requireNonNull(ctx, "ChannelHandlerContext must be not null");
        this.group = Objects.requireNonNull(group, "group must be not null");
        this.topic = Objects.requireNonNull(topic, "topic must be not null");
        this.key = (key == null ? "*" : key);
        this.createTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscribe subscribe = (Subscribe) o;
        return Objects.equals(ctx, subscribe.ctx) &&
                Objects.equals(group, subscribe.group) &&
                Objects.equals(topic, subscribe.topic) &&
                Objects.equals(key, subscribe.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ctx, group, topic, key);
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public String getGroup() {
        return group;
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }

    public long getCreateTime() {
        return createTime;
    }


    static Consumer<Boolean> emptyConsumer = aBoolean -> {};
    public ChannelFuture writeAndFlush(Object msg){
        return writeAndFlush(msg, emptyConsumer);
    }
    public ChannelFuture writeAndFlush(Object msg, Consumer<Boolean> result){
        writeCount.incrementAndGet();
        return getCtx().writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                try {
                    if (future.isSuccess()) {
                        writeCountSuccess.incrementAndGet();
                    } else {
                        writeCountFailed.incrementAndGet();
                        logger.warn("write msg failed to {}", future.channel());
                    }
                } finally {
                    result.accept(future.isSuccess());
                }
            }
        });
    }
    public long getWriteCount() {
        return writeCount.get();
    }

    public long getWriteCountFailed() {
        return writeCountFailed.get();
    }

    public long getWriteCountSuccess() {
        return writeCountSuccess.get();
    }

    public Subscribe writeCountSuccess(long v) {
        writeCountSuccess.addAndGet(v);
        return this;
    }
    public Subscribe writeCountFailed(long v) {
        writeCountFailed.addAndGet(v);
        return this;
    }
    public Subscribe writeCount(long v) {
        writeCount.addAndGet(v);
        return this;
    }
    final static SubscribeComparator comparator = new SubscribeComparator();
    public static Comparator<Subscribe> getComparator() {
        return comparator;
    }
    final static class SubscribeComparator implements Comparator<Subscribe> {
        @Override
        public int compare(Subscribe o1, Subscribe o2) {
            int v = new Long(o1.getWriteCountSuccess()).compareTo(o2.getWriteCountSuccess());
            if(v == 0) {
                v = new Long(o1.getWriteCount()).compareTo(o2.getWriteCount());
            }
            if(v == 0) {
                v = new Long(o2.getWriteCountFailed()).compareTo(o1.getWriteCountFailed());
            }
            return v;
        }
    }

    @Override
    public String toString() {
        return "Subscribe{" +
                "group='" + group + '\'' +
                ", topic='" + topic + '\'' +
                ", key='" + key + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}