package com.stony.mc.handler;

import com.stony.mc.listener.IdleStateEventListener;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.future.ResultFutureStore;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *<pre>
 *  {@link io.netty.channel.ChannelInboundHandler}
 *  {@link io.netty.channel.ChannelInboundHandlerAdapter}
 *</pre>
 * @author stony
 * @version 上午11:02
 * @since 2019/1/3
 */
public class ClientHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    final ResultFutureStore futureStore;
    final IdleStateEventListener idleStateEventListener;

    public ClientHandler(ResultFutureStore futureStore) {
        this(futureStore, null);
    }
    public ClientHandler(ResultFutureStore futureStore, IdleStateEventListener idleStateEventListener) {
        super();
        this.futureStore = futureStore;
        this.idleStateEventListener = idleStateEventListener;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol value) throws Exception {
        logger.debug("client: {}", value);
        this.futureStore.update(value.getId(), value);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("服务已断开: {}", ctx);
        if(idleStateEventListener != null) {
            idleStateEventListener.onIdleEvent(ctx, null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.futureStore.remove(ctx);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            if(idleStateEventListener != null) {
                idleStateEventListener.onIdleEvent(ctx, (IdleStateEvent) evt);
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}