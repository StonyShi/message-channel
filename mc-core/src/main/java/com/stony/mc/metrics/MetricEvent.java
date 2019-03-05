package com.stony.mc.metrics;

/**
 * <p>mc-core
 * <p>com.stony.mc.metrics
 *
 * @author stony
 * @version 上午10:43
 * @since 2019/1/14
 */
public enum MetricEvent {

    CONNECTION("连接"),
    DISCONNECT("断开连接"),
    CONNECTION_ERROR("连接错误"),
    DISCARD_CONNECTION("拒绝连接"),
    REQUEST("请求"),
    REQUEST_BYTE_SIZE("请求字节大小"),
    REQUEST_SUCCEED("请求成功"),
    REQUEST_FAILED("请求失败"),
    RESPONSE("响应"),
    RESPONSE_BYTE_SIZE("响应字节"),
    RESPONSE_SUCCEED("响应成功"),
    RESPONSE_FAILED("响应失败"),
    RESPONSE_TIME_MS("响应时长"),
    SUBSCRIBER("订阅者"),
    SUBSCRIBED_MESSAGE("订阅者的消息"),
    SUBSCRIBED_MESSAGE_SUCCEED("订阅者的消息成功"),
    SUBSCRIBED_MESSAGE_FAILED("订阅者的消息失败"),
    UNSUBSCRIBE("取消订阅");

    String name;
    MetricEvent(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}