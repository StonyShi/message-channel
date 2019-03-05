package com.stony.mc.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午5:26
 * @since 2019/1/29
 */
public interface BaseCluster {

    default BaseClient getMasterLive() throws ConnectException {
        HostPort[] masterHostPorts = getMasterHostPorts();
        BaseClient master = new BaseClient(){};
        HostPort masterServer = masterHostPorts[0];
        for (int i = 0, len = masterHostPorts.length; i < len; i++) {
            masterServer = masterHostPorts[i];
            getLogger().info("{} connect to Master[{}:{}].", getName(), masterServer.getHost(), masterServer.getPort());
            try {
                master.connect(masterServer.getHost(), masterServer.getPort(), getMasterReties());
                break;
            } catch (Exception e) {
                getLogger().info("{} Failed to Connect Master[{}:{}].", getName(), masterServer.getHost(), masterServer.getPort());
            }
        }
        if (!master.isLive()) {
            master.shutdown();
            throw new ConnectException(String.format("%s connect to Master[%s:%d] Failed.",
                    getName(), masterServer.getHost(), masterServer.getPort()));
        }
        return master;
    }

    Logger getLogger();
    HostPort[] getMasterHostPorts();
    int getMasterReties();
    String getName();

}