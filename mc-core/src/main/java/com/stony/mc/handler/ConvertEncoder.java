package com.stony.mc.handler;

import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureStore;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *  ResultFuture convert ExchangeProtocol
 *  Outbound
 *  {@link io.netty.channel.ChannelOutboundHandler}
 *  {@link io.netty.channel.ChannelOutboundHandlerAdapter}
 * @author stony
 * @version 下午4:19
 * @since 2019/1/4
 */
@ChannelHandler.Sharable
public class ConvertEncoder extends MessageToMessageEncoder<ResultFuture> {

    final ResultFutureStore futureStore;

    public ConvertEncoder(ResultFutureStore futureStore) {
        super();
        this.futureStore = futureStore;
    }

    public ConvertEncoder(Class<? extends ResultFuture> outboundMessageType, ResultFutureStore futureStore) {
        super(outboundMessageType);
        this.futureStore = futureStore;
    }

    /**
     * ResultFuture convert ExchangeProtocol
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, ResultFuture msg, List<Object> out) throws Exception {
        msg.setContext(ctx);
        this.futureStore.put(msg);
        out.add(msg.getRequest());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.futureStore.remove(ctx);
        cause.printStackTrace();
        ctx.close();
    }
}