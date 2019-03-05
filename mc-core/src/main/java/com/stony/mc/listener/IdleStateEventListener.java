package com.stony.mc.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 上午10:39
 * @since 2019/1/16
 */
public interface IdleStateEventListener {
    void onIdleEvent(ChannelHandlerContext ctx, IdleStateEvent event);
}