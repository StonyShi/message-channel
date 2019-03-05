package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeProtocolContext;
import com.stony.mc.protocol.ExchangeResponseConsumer;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午5:07
 * @since 2019/1/21
 */
public interface BusinessHandler extends SubscribeListener {

    @Override
    default void onSubscribe(ExchangeProtocol value) {
        if(support(value.getType())  && value instanceof ExchangeResponseConsumer) {
            final ExchangeResponseConsumer context = (ExchangeResponseConsumer) value;
            ExchangeProtocol response = handle(context);
            context.accept(context.getCtx(), response);
        }
    }

    ExchangeProtocol handle(ExchangeProtocolContext request);
}