package com.stony.mc.manager;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>message-channel
 * <p>com.stony.mc.manager
 *
 * @author stony
 * @version 上午11:31
 * @since 2019/1/24
 */
public class RegisterInfoHolder implements Serializable{
    private static final long serialVersionUID = 2318803120108944582L;

    transient final ChannelHandlerContext ctx;
    final RegisterInfo info;
    public RegisterInfoHolder(ChannelHandlerContext ctx, RegisterInfo info) {
        this.ctx = ctx;
        this.info = info;
    }
    public static RegisterInfoHolder wrap(ChannelHandlerContext ctx, RegisterInfo info) {
        return new RegisterInfoHolder(ctx, info);
    }
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public RegisterInfo getInfo() {
        return info;
    }
    public boolean isLive() {
        return ctx != null && ctx.channel() != null && ctx.channel().isOpen();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterInfoHolder that = (RegisterInfoHolder) o;
        return Objects.equals(this.getInfo().getUid(), that.getInfo().getUid()) &&
                Objects.equals(this.getInfo().getDevice(), that.getInfo().getDevice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.getUid(), info.getDevice());
    }

    @Override
    public String toString() {
        return "RegisterInfoHolder{" +
                "ctx=" + ctx +
                ", info=" + info +
                '}';
    }
}
