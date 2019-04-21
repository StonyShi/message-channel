package com.stony.mc.session;

import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureListenable;
import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 * 线程安全的客户端连接池
 * @author stony
 * @version 下午18:10
 * @since 2019/4/21
 */
public class PoolClient extends BaseClient<PoolClient>{

    private int clientPoolSize = 1;
    private List<Channel> originChannelList = new ArrayList<>(8);
    private int originChannelCount = 0;

    public PoolClient(int clientPoolSize) {
        super();
        this.clientPoolSize = clientPoolSize;
    }

    public PoolClient(int timeoutMs, int clientPoolSize) {
        super(timeoutMs);
        this.clientPoolSize = clientPoolSize;
    }

    @Override
    protected ChannelFuture clientConnect(Bootstrap client, String serverHost, int serverPort) {
        List<ChannelFuture> channelFutureList = new ArrayList<>(clientPoolSize);
        for (int i = 0; i < clientPoolSize; i++) {
            channelFutureList.add(super.clientConnect(client, serverHost, serverPort));
        }
        //TODO 优化连接
        for (ChannelFuture cf : channelFutureList) {
            try {
                originChannelList.add(cf.sync().channel());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        originChannelCount = originChannelList.size();
        return channelFutureList.get(0);
    }

    /**
     * 默认随机
     * @return Channel
     */
    public Channel selectedChannel() {
        return originChannelList.get(ThreadLocalRandom.current().nextInt(0, originChannelCount));
    }
    @Override
    public ChannelFuture execute(ExchangeProtocol value) {
        return selectedChannel().writeAndFlush(value);
    }
    public ResultFuture submit(ExchangeProtocol value) {
        ResultFuture future = ResultFuture.wrap(value);
        selectedChannel().writeAndFlush(future);
        return future;
    }
    public ResultFutureListenable invoke(ExchangeProtocol value) {
        ResultFutureListenable future = ResultFutureListenable.wrap(value);
        selectedChannel().writeAndFlush(future);
        return future;
    }
}