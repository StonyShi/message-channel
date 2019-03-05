package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 上午9:55
 * @since 2019/1/22
 */
public interface BalanceListener extends SubscribeListener{

    default void onSubscribe(ExchangeProtocol value) {
        if(support(value.getType())) {
            onBalance(Integer.valueOf(value.getBody().getFormatValue()));
        }
    }

    void onBalance(Integer value);

    default boolean support(ExchangeTypeEnum typeEnum) {
        return ExchangeTypeEnum.BALANCE == typeEnum;
    }
}
