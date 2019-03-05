package com.stony.mc.protocol;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;

/**
 * <p>message-channel
 * <p>com.stony.mc.protocol
 *
 * @author stony
 * @version 下午5:48
 * @since 2019/1/21
 */
public abstract class ExchangeResponseConsumer extends ExchangeProtocolContext implements BiConsumer<ChannelHandlerContext, ExchangeProtocol>{
    private static final long serialVersionUID = 1129081974624357591L;
    public ExchangeResponseConsumer(ExchangeProtocol value, ChannelHandlerContext ctx) {
        super(value, ctx);
    }
}