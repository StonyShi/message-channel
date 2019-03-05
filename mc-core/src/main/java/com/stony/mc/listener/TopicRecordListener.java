package com.stony.mc.listener;

import com.stony.mc.protocol.ExchangeBody;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午6:06
 * @since 2019/1/18
 */
public interface TopicRecordListener extends SubscribeListener {
    @Override
    default void onSubscribe(ExchangeProtocol value) {
        if (value.getStatus().isOk()) {
            ExchangeBody body = value.getBody();
            onSubscribe(new TopicRecord(value.getId(), body.getName(), body.getKey(), body.getFormatValue()));
        } else {
            onSubscribe(new TopicRecord(value.getId(), value.getStatus().getReasonPhrase(), value.getStatus().getCode()));
        }
    }
    void onSubscribe(TopicRecord topicRecord);
    @Override
    default boolean support(ExchangeTypeEnum typeEnum) {
        return true;
    }
}