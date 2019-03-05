package com.stony.mc.session;

import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureListenable;
import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.channel.ChannelFuture;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午5:29
 * @since 2019/1/15
 */
public interface MCClient<T extends MCClient> {
    T connect() throws Exception;

    T connect(String serverHost, int serverPort) throws Exception;

    T connect(int reties) throws Exception;

    T connect(String serverHost, int serverPort, int reties) throws Exception;

    T connect(String serverHost, int serverPort, int reties, long delayTimeMS) throws Exception;

    ChannelFuture execute(ExchangeProtocol value);

    ResultFuture submit(ExchangeProtocol value);

    ResultFutureListenable invoke(ExchangeProtocol value);

    void shutdown();

    void waitTillShutdown();

    boolean isLive();
}
