package com.stony.mc.session;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午6:07
 * @since 2019/1/15
 */
public class HostPort {
    private String host;
    private int port;

    public static HostPort wrap(String host, int port) {
        return new HostPort(host, port);
    }

    @JSONCreator
    public HostPort(@JSONField(name = "host") String host, @JSONField(name = "port") int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String line() {
        return host + ":" +  port;
    }
    public String toJson() {
        return "{" +
                "host:'" + host + '\'' +
                ", port:" + port +
                '}';
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostPort hostPort = (HostPort) o;
        return port == hostPort.port && Objects.equals(host, hostPort.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
