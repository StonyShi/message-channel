package com.stony.mc.future;

import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午3:08
 * @since 2019/1/4
 */
public interface ResultFutureStore<V extends ExchangeProtocol> extends AutoCloseable {

    void put(ResultFuture future);
    void update(long key, V value);
    void update(long key, V value, ChannelHandlerContext ctx);
    ResultFuture get(long key);
    ResultFuture del(long key);

    void shutdown();

    void remove(ChannelHandlerContext ctx);

    void setSubscribeListener(SubscribeListener<V> subscribeListener);
    SubscribeListener<V> getSubscribeListener();
}