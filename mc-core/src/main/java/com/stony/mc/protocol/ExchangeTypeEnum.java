package com.stony.mc.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午2:58
 * @since 2019/1/3
 */
public enum ExchangeTypeEnum {

    PING(0),
    NOTIFY(1),
    CHAT(2),
    MESSAGE(3),
    SUBSCRIBE(4),
    UNSUBSCRIBE(5),
    /** 分配 **/
    ALLOCATION(6),
    BALANCE(7),
    /** 绑定 **/
    BINDING(8),
    /** 注册 members **/
    REGISTER(9),
    /** 注册的 members **/
    REGISTERED(10),
    ACK(13);

    int code;
    ExchangeTypeEnum(int code) {
        this.code = code;
    }
    static Map<Integer,ExchangeTypeEnum> INDEX;
    static {
        INDEX = new HashMap<>(8);
        for (ExchangeTypeEnum v : ExchangeTypeEnum.values()) {
            INDEX.put(v.code, v);
        }
    }
    public static ExchangeTypeEnum byCode(int code) {
        return INDEX.get(code);
    }
    public int getCode() {
        return code;
    }
}