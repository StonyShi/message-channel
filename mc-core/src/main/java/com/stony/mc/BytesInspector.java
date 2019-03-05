package com.stony.mc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午9:56
 * @since 2019/1/3
 */
@ChannelHandler.Sharable
public class BytesInspector extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(BytesInspector.class);
    EventsSubject eventsSubject;

    public BytesInspector(EventsSubject eventsSubject) {
        this.eventsSubject = eventsSubject;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (ByteBuf.class.isAssignableFrom(msg.getClass())) {
                publishBytesRead((ByteBuf) msg);
            } else if (ByteBufHolder.class.isAssignableFrom(msg.getClass())) {
                ByteBufHolder holder = (ByteBufHolder) msg;
                publishBytesRead(holder.content());
            }
        } catch (Exception e) {
            logger.warn("Failed to publish bytes read metrics event. This does *not* stop the pipeline processing.", e);
        } finally {
            super.channelRead(ctx, msg);
        }
    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (ByteBuf.class.isAssignableFrom(msg.getClass())) {
                publishBytesWritten(((ByteBuf) msg).readableBytes(), promise);
            } else if (ByteBufHolder.class.isAssignableFrom(msg.getClass())) {
                publishBytesWritten(((ByteBufHolder)msg).content().readableBytes(), promise);
            } else if (FileRegion.class.isAssignableFrom(msg.getClass())) {
                publishBytesWritten(((FileRegion) msg).count(), promise);
            }
        } catch (Exception e) {
            logger.warn("Failed to publish bytes write metrics event. This does *not* stop the pipeline processing.", e);
        } finally {
            super.write(ctx, msg, promise);
        }
    }

    @SuppressWarnings("unchecked")
    protected void publishBytesWritten(final long bytesToWrite, ChannelPromise promise) {
        final long startTimeMillis = ClockUtils.newStartTimeMillis();
        eventsSubject.onEvent("WriteStartEvent", 0, bytesToWrite);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    eventsSubject.onEvent("WriteSuccessEvent",
                            ClockUtils.onEndMillis(startTimeMillis), bytesToWrite);
                } else {
                    eventsSubject.onEvent("WriteFailedEvent",
                            ClockUtils.onEndMillis(startTimeMillis), future.cause(), bytesToWrite);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void publishBytesRead(ByteBuf byteBuf) {
        if (null != byteBuf) {
            eventsSubject.onEvent("BytesReadEvent", 0,  byteBuf.readableBytes());
        }
    }
}
