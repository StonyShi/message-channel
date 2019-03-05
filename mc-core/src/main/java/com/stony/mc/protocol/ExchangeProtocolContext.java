package com.stony.mc.protocol;

import com.stony.mc.NetUtils;
import com.stony.mc.session.HostPort;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;

/**
 * <p>mc-core
 * <p>com.stony.mc.protocol
 *
 * @author stony
 * @version 下午4:31
 * @since 2019/1/14
 */
public class ExchangeProtocolContext extends ExchangeProtocol {
    private static final long serialVersionUID = 1560536671900069154L;
    final HostPort remoteAddress;
    final ChannelHandlerContext ctx;
    public ExchangeProtocolContext(ExchangeProtocol value, ChannelHandlerContext ctx) {
        super(value.magic, value.id, value.type, value.compress, value.status, value.bodyLen);
        this.body = value.body;
        this.ctx = ctx;
        this.remoteAddress = NetUtils.getChannelHostPort(ctx);
    }

    public HostPort getRemoteAddress() {
        return remoteAddress;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    public String toString() {
        return "ExchangeProtocolContext{" +
                "magic=" + magic +
                ", id=" + id +
                ", type=" + type +
                ", compress=" + compress +
                ", status=" + status +
                ", bodyLen=" + bodyLen +
                ", body=" + body +
                ", remoteAddress=" + remoteAddress +
                '}';
    }
}