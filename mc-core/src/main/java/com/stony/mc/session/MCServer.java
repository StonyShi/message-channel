package com.stony.mc.session;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午5:24
 * @since 2019/1/15
 */
public interface MCServer {
    String getServerName();
    int getServerPort();
    Closeable startup() throws Exception;
    void startAndWait(long timeout, TimeUnit unit) throws Exception;
    void startAndWait() throws Exception;
    void shutdown();
    boolean isLive();
}