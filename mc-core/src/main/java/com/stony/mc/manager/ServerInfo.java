package com.stony.mc.manager;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * <p>message-channel
 * <p>com.stony.mc.metrics
 *
 * @author stony
 * @version 下午3:58
 * @since 2019/1/15
 */
public class ServerInfo implements Serializable{
    private static final long serialVersionUID = -5181020939295161847L;

    @JSONField(name = "machine_id")
    private String machineId;
    @JSONField(name = "server_name")
    private String serverName;
    @JSONField(name = "server_port")
    private int serverPort;
    @JSONField(name = "connection_count")
    private long connectionCount;
    @JSONField(name = "connection_error_count")
    private long connectionErrorCount;
    @JSONField(name = "max_connection_count")
    private long maxConnectionCount;
    @JSONField(name = "request_total_count")
    private long requestTotalCount;
    @JSONField(name = "request_total_bytes")
    private long requestTotalBytes;
    @JSONField(name = "response_total_count")
    private long responseTotalCount;
    @JSONField(name = "response_total_succeed_count")
    private long responseTotalSucceedCount;
    @JSONField(name = "response_total_failed_count")
    private long responseTotalFailedCount;
    @JSONField(name = "response_total_time_ms")
    private long responseTotalTimeMs;
    @JSONField(name = "response_total_bytes")
    private long responseTotalBytes;
    @JSONField(name = "subscriber_count")
    private long subscriberCount;
    @JSONField(name = "subscriber_message_count")
    private long subscriberMessageCount;
    @JSONField(name = "created_time")
    private long createdTime;

    @JSONField(name = "env")
    private ServerEnvironment env;

    @JSONCreator
    public ServerInfo(@JSONField(name = "server_name") String serverName,
                      @JSONField(name = "server_port") int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public ServerInfo() {
        this.createdTime = System.currentTimeMillis();
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(long connectionCount) {
        this.connectionCount = Math.max(0, connectionCount);;
    }

    public long getConnectionErrorCount() {
        return connectionErrorCount;
    }

    public void setConnectionErrorCount(long connectionErrorCount) {
        this.connectionErrorCount = Math.max(0, connectionErrorCount);
    }

    public long getMaxConnectionCount() {
        return maxConnectionCount;
    }

    public void setMaxConnectionCount(long maxConnectionCount) {
        this.maxConnectionCount = maxConnectionCount;
    }

    public long getRequestTotalCount() {
        return requestTotalCount;
    }

    public void setRequestTotalCount(long requestTotalCount) {
        this.requestTotalCount = requestTotalCount;
    }

    public long getRequestTotalBytes() {
        return requestTotalBytes;
    }

    public void setRequestTotalBytes(long requestTotalBytes) {
        this.requestTotalBytes = requestTotalBytes;
    }

    public long getResponseTotalCount() {
        return responseTotalCount;
    }

    public void setResponseTotalCount(long responseTotalCount) {
        this.responseTotalCount = responseTotalCount;
    }

    public long getResponseTotalSucceedCount() {
        return responseTotalSucceedCount;
    }

    public void setResponseTotalSucceedCount(long responseTotalSucceedCount) {
        this.responseTotalSucceedCount = responseTotalSucceedCount;
    }

    public long getResponseTotalFailedCount() {
        return responseTotalFailedCount;
    }

    public void setResponseTotalFailedCount(long responseTotalFailedCount) {
        this.responseTotalFailedCount = responseTotalFailedCount;
    }

    public long getResponseTotalTimeMs() {
        return responseTotalTimeMs;
    }

    public void setResponseTotalTimeMs(long responseTotalTimeMs) {
        this.responseTotalTimeMs = responseTotalTimeMs;
    }

    public long getResponseTotalBytes() {
        return responseTotalBytes;
    }

    public void setResponseTotalBytes(long responseTotalBytes) {
        this.responseTotalBytes = responseTotalBytes;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public long getSubscriberMessageCount() {
        return subscriberMessageCount;
    }

    public void setSubscriberMessageCount(long subscriberMessageCount) {
        this.subscriberMessageCount = subscriberMessageCount;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public ServerEnvironment getEnv() {
        return env;
    }

    public void setEnv(ServerEnvironment env) {
        this.env = env;
    }

    public String getAddress() {
        return String.format("%s:%d", getServerName(), getServerPort());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return serverPort == that.serverPort &&
                Objects.equals(serverName, that.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, serverPort);
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "machineId='" + machineId + '\'' +
                ", serverName='" + serverName + '\'' +
                ", serverPort=" + serverPort +
                ", connectionCount=" + connectionCount +
                ", connectionErrorCount=" + connectionErrorCount +
                ", maxConnectionCount=" + maxConnectionCount +
                ", requestTotalCount=" + requestTotalCount +
                ", requestTotalBytes=" + requestTotalBytes +
                ", responseTotalCount=" + responseTotalCount +
                ", responseTotalSucceedCount=" + responseTotalSucceedCount +
                ", responseTotalFailedCount=" + responseTotalFailedCount +
                ", responseTotalTimeMs=" + responseTotalTimeMs +
                ", responseTotalBytes=" + responseTotalBytes +
                ", subscriberCount=" + subscriberCount +
                ", subscriberMessageCount=" + subscriberMessageCount +
                ", env=" + env +
                ", createdTime=" + createdTime +
                '}';
    }
    final static ServerInfoComparator comparator = new ServerInfoComparator();
    public static Comparator<ServerInfo> getComparator() {
        return comparator;
    }
    final static class ServerInfoComparator implements Comparator<ServerInfo> {
        @Override
        public int compare(ServerInfo o1, ServerInfo o2) {
            int v = new Long(o1.getConnectionCount()).compareTo(o2.getConnectionCount());
            if(v == 0) {
                v = new Long(o1.getRequestTotalCount()).compareTo(o2.getRequestTotalCount());
            }
            if(v == 0) {
                v = new Long(o1.getRequestTotalBytes()).compareTo(o2.getRequestTotalBytes());
            }
            return v;
        }
    }
}
