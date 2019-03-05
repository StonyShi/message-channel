package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

/**
 * <p>mc-core
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午3:59
 * @since 2019/1/8
 */
public interface SubscribeListener<T extends ExchangeProtocol> {
    void onSubscribe(T t);
    default boolean support(ExchangeTypeEnum typeEnum) {
        return false;
    }
}