package com.stony.mc.manager;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>message-channel
 * <p>com.stony.mc.manager
 *
 * @author stony
 * @version 下午6:15
 * @since 2019/1/23
 */
public class RegisterInfo implements Serializable {
    private static final long serialVersionUID = 4868627847365836145L;

    @JSONField(name = "uid")
    private String uid;
    private String device;
    private String address;     //host:port
    private Set<String> tags;
    private Set<String> alias;
    @JSONField(name = "created_time")
    private long createdTime;

    @JSONField(name = "worker_host")
    private String workerHost;
    @JSONField(name = "worker_port")
    private int workerPort;


    public RegisterInfo(String uid) {
        this(uid, null);
    }
    public RegisterInfo(String uid, String device) {
        this.uid = uid;
        this.device = device;
        this.tags = new HashSet<>(16);
        this.alias = new HashSet<>(16);
        this.createdTime = System.currentTimeMillis();
    }
    @JSONCreator
    private RegisterInfo() {
    }

    public String getUid() {
        return uid;
    }

    public String getAddress() {
        return address;
    }

    public String getWorkerAddress() {
        return workerHost+":"+workerPort;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Set<String> getAlias() {
        return alias;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
    }
    public void removeTags(Collection<String> tags) {
        this.tags.removeAll(tags);
    }

    public void addAlias(Collection<String> alias) {
        this.alias.addAll(alias);
    }
    public void removeAlias(Collection<String> alias) {
        this.alias.removeAll(alias);
    }
    public void addTag(String tag){
        this.tags.add(tag);
    }
    public void removeTag(String tag){
        this.tags.remove(tag);
    }
    public void addAlias(String alias) {
        this.alias.add(alias);
    }
    public void removeAlias(String alias) {
        this.alias.remove(alias);
    }

    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    public void setAlias(Set<String> alias) {
        this.alias = alias;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWorkerHost() {
        return workerHost;
    }

    public void setWorkerHost(String workerHost) {
        this.workerHost = workerHost;
    }

    public int getWorkerPort() {
        return workerPort;
    }

    public void setWorkerPort(int workerPort) {
        this.workerPort = workerPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterInfo that = (RegisterInfo) o;
        return Objects.equals(uid, that.uid) &&
                Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, device);
    }

    @Override
    public String toString() {
        return "RegisterInfo{" +
                "uid='" + uid + '\'' +
                ", address='" + address + '\'' +
                ", device='" + device + '\'' +
                ", workerAddress='" + getWorkerAddress() + '\'' +
                ", tags=" + tags +
                ", alias=" + alias +
                ", createdTime=" + createdTime +
                '}';
    }
}