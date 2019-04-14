package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

public interface ChatListener extends SubscribeListener {

    default void onSubscribe(ExchangeProtocol value) {
        if(support(value.getType())) {
            onChat(value);
        }
    }
    void onChat(ExchangeProtocol value);

    default boolean support(ExchangeTypeEnum typeEnum) {
        return ExchangeTypeEnum.CHAT == typeEnum;
    }
}