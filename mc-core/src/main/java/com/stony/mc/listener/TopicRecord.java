package com.stony.mc.listener;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 下午6:08
 * @since 2019/1/18
 */
public class TopicRecord {
    long id;
    String topic;
    String key;
    String record;
    String error;
    int errorCode = -1;

    public TopicRecord(long id, String error, int errorCode) {
        this.id = id;
        this.error = error;
        this.errorCode = errorCode;
    }
    public TopicRecord(long id, String topic, String key, String record) {
        this.id = id;
        this.topic = topic;
        this.key = key;
        this.record = record;
    }
    public long getId() {
        return id;
    }
    public String getTopic() {
        return topic;
    }
    public String getKey() {
        return key;
    }
    public String getRecord() {
        return record;
    }
    public boolean isOk() {
        return errorCode == -1;
    }

    @Override
    public String toString() {
        if (isOk()) {
            return "TopicRecord{" +
                    "id=" + id +
                    ", topic='" + topic + '\'' +
                    ", key='" + key + '\'' +
                    ", record='" + record + '\'' +
                    '}';
        }
        return "TopicRecord{" +
                "id=" + id +
                ", error='" + error + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}