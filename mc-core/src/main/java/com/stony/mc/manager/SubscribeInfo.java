package com.stony.mc.manager;

import java.util.UUID;

/**
 * <p>mc-core
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午6:26
 * @since 2019/1/8
 */
public class SubscribeInfo {
    private String topic;
    private String key;
    private String group;

    public SubscribeInfo(String group, String topic, String key) {
        this.group = (group == null ? UUID.randomUUID().toString() : group);
        this.topic = topic;
        this.key = key;
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "SubscribeInfo{" +
                "topic='" + topic + '\'' +
                ", key='" + key + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
