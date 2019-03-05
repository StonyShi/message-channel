package com.stony.mc.session;

import com.stony.mc.NetUtils;
import com.stony.mc.ResourceManger;
import com.stony.mc.Utils;
import com.stony.mc.concurrent.TaskExecutorFactory;
import com.stony.mc.handler.ExchangeCodecAdapter;
import com.stony.mc.handler.ServerHandler;
import com.stony.mc.manager.ChannelManager;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.metrics.MetricStatistics;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>mc-core
 * <p>com.stony.mc.server
 *
 * @author stony
 * @version 下午4:56
 * @since 2019/1/10
 */
public abstract class BaseServer<T extends BaseServer> implements MCServer, Closeable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String serverName;
    private int serverPort;
    private int ioThreads = (Runtime.getRuntime().availableProcessors() * 2) - 1;
    private int serviceThreads = 25;
    private int serviceMaxThreads = 25;
    private int serviceThreadLiveMS = 60000;
    private AtomicBoolean startup = new AtomicBoolean();
    private AtomicBoolean shutdown = new AtomicBoolean();
    private CountDownLatch downLatch = new CountDownLatch(1);
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private Channel originChannel;
    private ChannelId channelId;
    private ThreadPoolExecutor executor;
    private SubscribeListener businessHandler;
    private MetricStatistics metricStatistics;

    private ChannelManager channelManager;

    public BaseServer(String serverName, int serverPort) {
        this.serverName = NetUtils.getMcServerName(serverName);
        this.serverPort = serverPort;
    }

    public BaseServer(int serverPort) {
        this(null, serverPort);
    }

    public T ioThreads(int ioThreads) {
        this.ioThreads = ioThreads;
        return (T) this;
    }

    public T serviceThreads(int serviceThreads) {
        this.serviceThreads = serviceThreads;
        return (T) this;
    }
    public T serviceMaxThreads(int serviceMaxThreads) {
        this.serviceMaxThreads = serviceMaxThreads;
        return (T) this;
    }
    public T serviceThreadLiveMS(int serviceThreadLiveMS) {
        this.serviceThreadLiveMS = serviceThreadLiveMS;
        return (T) this;
    }
    public T subscribeListener(SubscribeListener businessHandler) {
        this.businessHandler = businessHandler;
        return (T) this;
    }

    public T metricStatistics(MetricStatistics metricStatistics) {
        this.metricStatistics = metricStatistics;
        return (T) this;
    }

    public void setMetricStatistics(MetricStatistics metricStatistics) {
        this.metricStatistics = metricStatistics;
    }

    public T setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
        return (T) this;
    }

    @Override
    public String getServerName() {
        return this.serverName;
    }

    @Override
    public int getServerPort() {
        return this.serverPort;
    }

    public Closeable startup() throws Exception {
        if (startup.compareAndSet(false, true)) {
            this.boss = new NioEventLoopGroup();
            this.worker = new NioEventLoopGroup(ioThreads);
            if(channelManager == null) {
                this.channelManager = ChannelManager.create();
            }

            String ne = Utils.isEmpty(serverName) ? getClass().getSimpleName() : serverName;
            this.executor = TaskExecutorFactory.getElasticExecutor(ne, serviceThreads, serviceMaxThreads, serviceThreadLiveMS);

            ServerBootstrap server = new ServerBootstrap()
                    .group(boss, worker).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ExchangeCodecAdapter adapter = new ExchangeCodecAdapter();
                            ServerHandler serverHandler = new ServerHandler(channelManager, executor, businessHandler, metricStatistics);
                            socketChannel.pipeline()
//                            .addFirst("BytesInspector", new BytesInspector(new EventsSubject()))
                                    .addLast("ExchangeEncoder", adapter.getEncoder())  //out
                                    .addLast("ExchangeDecoder", adapter.getDecoder())  //in
                                    .addLast("ServerHandler", serverHandler)     //in
                            ;
                        }
                    });

            ChannelFuture future = Utils.isEmpty(serverName) ?
                    server.bind(serverPort).sync() :
                    server.bind(serverName, serverPort).sync();
            this.originChannel = future.channel();
            this.channelId = originChannel.id();
            logger.info("Startup server : {}", originChannel.toString());
        }
        return this;
    }

    public void startAndWait(long timeout, TimeUnit unit) throws Exception {
        startup();
        ResourceManger.register(new ResourceManger.ResourceShutdownListener() {
            @Override
            public void shutdown() {
                BaseServer.this.shutdown();
            }
        });
        downLatch.await(timeout, unit);
    }
    public void startAndWait() throws Exception {
        startup();
        ResourceManger.register(new ResourceManger.ResourceShutdownListener() {
            @Override
            public void shutdown() {
                BaseServer.this.shutdown();
            }
        });
        downLatch.await();
    }

    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            logger.info("Shutdown server : {}", originChannel);
            try {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            } finally {
                channelManager.shutdown();
                downLatch.countDown();
            }
        }
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public boolean isLive() {
        return startup.get() && originChannel != null && originChannel.isOpen();
    }
    @Override
    public void close() throws IOException {
        shutdown();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public MetricStatistics getMetricStatistics() {
        return metricStatistics;
    }
    public String getAddress() {
        return serverName+":"+serverPort;
    }
}