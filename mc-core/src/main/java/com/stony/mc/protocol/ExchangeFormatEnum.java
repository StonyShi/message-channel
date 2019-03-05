package com.stony.mc.protocol;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>mc-core
 * <p>com.stony.mc
 * Serialization
 * @author stony
 * @version 下午12:14
 * @since 2019/1/3
 */
public enum ExchangeFormatEnum {
    NONE(0) {
        @Override
        <T> byte[] serialize(T t) {
            return new byte[0];
        }
    }, TEXT(1) {
        @Override
        <T> byte[] serialize(T t) {
            return t.toString().getBytes(StandardCharsets.UTF_8);
        }
    }, JSON(2) {
        @Override
        <T> byte[] serialize(T t) {
            return com.alibaba.fastjson.JSONObject.toJSONBytes(t);
        }
    }, IMAGE(3) {
        @Override
        <T> byte[] serialize(T t) {
            return new byte[0];
        }
    }, VIDEO(4) {
        @Override
        <T> byte[] serialize(T t) {
            return new byte[0];
        }
    };
    int code;
    ExchangeFormatEnum(int code) {
        this.code = code;
    }
    static Map<Integer,ExchangeFormatEnum> INDEX;
    static {
        INDEX = new HashMap<>(8);
        for (ExchangeFormatEnum v : ExchangeFormatEnum.values()) {
            INDEX.put(v.code, v);
        }
    }
    public static ExchangeFormatEnum byCode(int code) {
        return INDEX.get(code);
    }

    public int getCode() {
        return code;
    }

    abstract <T> byte[] serialize(T t);
//    abstract <T> T deserialize(byte[] data, Class<T> clazz);
}