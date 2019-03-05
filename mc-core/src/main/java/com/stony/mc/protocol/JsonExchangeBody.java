package com.stony.mc.protocol;

import java.nio.charset.StandardCharsets;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:43
 * @since 2019/1/8
 */
public class JsonExchangeBody extends BaseExchangeBody {
    JsonExchangeBody(ExchangeCompressEnum compress, String value, String name, String key) {
        this(compress, value.getBytes(StandardCharsets.UTF_8), name, key);
    }
    JsonExchangeBody(ExchangeCompressEnum compress, byte[] value, String name, String key) {
        super(compress, value, name, key);
    }
    @Override
    public ExchangeFormatEnum getFormat() {
        return ExchangeFormatEnum.JSON;
    }
}