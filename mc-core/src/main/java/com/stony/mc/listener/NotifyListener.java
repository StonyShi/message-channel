package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午16:10
 * @since 2019/4/21
 */
public interface NotifyListener extends SubscribeListener{
    @Override
    default void onSubscribe(ExchangeProtocol value) {
        if(support(value.getType())) {
            onNotify(value);
        }
    }
    //TODO 消息实体优化
    void onNotify(ExchangeProtocol value);

    default boolean support(ExchangeTypeEnum typeEnum) {
        return ExchangeTypeEnum.NOTIFY == typeEnum;
    }
}