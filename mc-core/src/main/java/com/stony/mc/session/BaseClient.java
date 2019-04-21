package com.stony.mc.session;

import com.stony.mc.ResourceManger;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureListenable;
import com.stony.mc.future.ResultFutureStore;
import com.stony.mc.future.SimpleResultFutureStore;
import com.stony.mc.handler.ClientHandler;
import com.stony.mc.handler.ConvertEncoder;
import com.stony.mc.handler.ExchangeCodecAdapter;
import com.stony.mc.listener.IdleStateEventListener;
import com.stony.mc.listener.ResultFutureListener;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.protocol.ExchangeProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>MessageCenter
 * <p>com.stony.mc.server
 *
 * @author stony
 * @version 下午4:51
 * @since 2019/1/11
 */
public abstract class BaseClient<T extends BaseClient> implements MCClient, IdleStateEventListener, Closeable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final long DEFAULT_DELAY_TIME_MS = 3000L;
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 3000;

    private String serverHost;
    private int serverPort;
    private int timeoutMs = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private AtomicBoolean shutdown = new AtomicBoolean();
    private AtomicBoolean connected = new AtomicBoolean();
    private AtomicBoolean inited = new AtomicBoolean();

    private  EventLoopGroup group;
    protected Channel originChannel;

    private final ResultFutureStore futureStore;

    protected int idleReadTime = 0;
    protected int idleWriteTime = 0;


    public BaseClient() {
        this(new SimpleResultFutureStore());
    }
    public BaseClient(int timeoutMs) {
        this(new SimpleResultFutureStore(), timeoutMs);
    }
    public BaseClient(ResultFutureStore futureStore) {
        this(futureStore, DEFAULT_CONNECT_TIMEOUT_MILLIS);
    }
    public BaseClient(ResultFutureStore futureStore, int timeoutMs) {
        this(null, 0, futureStore, timeoutMs);
    }
    public BaseClient(String serverHost, int serverPort, ResultFutureStore futureStore, int timeoutMs) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.futureStore = futureStore;
        this.timeoutMs = timeoutMs;
    }
    public T connect() throws Exception {
        return (T) this.connect(this.serverHost, this.serverPort);
    }
    public T connect(String serverHost, int serverPort) throws Exception {
        if(connected.compareAndSet(false, true) || !isLive()) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            Objects.requireNonNull(serverHost, "server host must be not null");
            if(serverPort <= 0) {
                throw new RuntimeException(String.format("server port[%d] must be great 0", serverPort));
            }
            try {
                if(inited.compareAndSet(false, true) ) {
                    this.group = new NioEventLoopGroup();
                }
                final IdleStateEventListener idleStateEventListener = this;

                Bootstrap client = new Bootstrap();
                client.group(group)
//                        .remoteAddress(serverHost, serverPort)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs);
                client.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ExchangeCodecAdapter adapter = new ExchangeCodecAdapter();
                        ConvertEncoder convert = new ConvertEncoder(futureStore);
                        ClientHandler clientHandler = new ClientHandler(futureStore, idleStateEventListener);
                        channel.pipeline()
                                .addLast("IdleStateHandler", new IdleStateHandler(idleReadTime, idleWriteTime, 0))
                                .addLast("ExchangeEncoder", adapter.getEncoder()) //out
                                .addLast("ConvertEncoder", convert)               //out
                                .addLast("ExchangeDecoder", adapter.getDecoder()) //in
                                .addLast("ClientHandler", clientHandler)          //in
                        ;
                    }
                });
                ChannelFuture future = this.clientConnect(client, serverHost, serverPort).sync();
                this.originChannel = future.channel();
                logger.info("Connect to [{}:{}] is succeed.", serverHost, serverPort);
            } catch (Exception e) {
                this.originChannel = null;
                connected.set(false);
                throw e;
            }
        }
        return (T) this;
    }
    protected ChannelFuture clientConnect(Bootstrap client, String serverHost, int serverPort) {
        return client.connect(serverHost, serverPort);
    }

    public T connect(int reties) throws Exception {
        return (T) connect(serverHost, serverPort, reties + 1, DEFAULT_DELAY_TIME_MS);
    }
    public T connect(String serverHost, int serverPort, int reties) throws Exception {
        return (T) connect(serverHost, serverPort, reties+1, DEFAULT_DELAY_TIME_MS);
    }
    public T connect(String serverHost, int serverPort, int reties, long delayTimeMS) throws Exception {
        return (T) doConnect(serverHost, serverPort, reties+1, delayTimeMS);
    }
    private T doConnect(String serverHost, int serverPort,int reties, long delayTimeMS) throws Exception {
        for (int i = 0; i < reties; i++) {
            try {
                connect(serverHost, serverPort);
                break;
            } catch (Exception e) {
                try {
                    long finalDelayTimeMs = delayTimeMS*((i + 1));
                    logger.info("Connect to [{}:{}] is failed, waiting for retry {} time {} ms.", serverHost, serverPort, i, finalDelayTimeMs);
                    TimeUnit.MILLISECONDS.sleep(finalDelayTimeMs);
                } catch (InterruptedException ev) {
                    logger.warn("Connect to [{}:{}] is failed, waiting for retry {} time, error:", serverHost, serverPort, i, ev.getMessage());
                }
            }
        }
        return (T) this;
    }

    public T connectTimeout(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return (T) this;
    }
    public T serverHost(String serverHost) {
        this.serverHost = serverHost;
        return (T) this;
    }
    public T serverPort(int serverPort) {
        this.serverPort = serverPort;
        return (T) this;
    }

    public T idleReadTime(int idleReadTime) {
        this.idleReadTime = idleReadTime;
        return (T) this;
    }

    public T idleWriteTime(int idleWriteTime) {
        this.idleWriteTime = idleWriteTime;
        return (T) this;
    }

    public void setIdleReadTime(int idleReadTime) {
        this.idleReadTime = idleReadTime;
    }

    public void setIdleWriteTime(int idleWriteTime) {
        this.idleWriteTime = idleWriteTime;
    }

    public ChannelFuture execute(ExchangeProtocol value) {
        return this.originChannel.writeAndFlush(value);
    }
    public ResultFuture submit(ExchangeProtocol value) {
        ResultFuture future = ResultFuture.wrap(value);
        this.originChannel.writeAndFlush(future);
        return future;
    }
    public ResultFutureListenable invoke(ExchangeProtocol value) {
        ResultFutureListenable future = ResultFutureListenable.wrap(value);
        this.originChannel.writeAndFlush(future);
        return future;
    }

    public void shutdown(){
        logger.info("Close client begin[{}]", getServerAddress());
        if(shutdown.compareAndSet(false, true)) {
            try {
                futureStore.shutdown();
            } finally {
                group.shutdownGracefully();
                inited.compareAndSet(true, false);
                connected.compareAndSet(true, false);
            }
            logger.info("Close client succeed[{}]", getServerAddress());
            shutdownFinished.countDown();
        }
    }

    final CountDownLatch shutdownFinished = new CountDownLatch(1);
    @Override
    public void waitTillShutdown() {
        ResourceManger.register(new ResourceManger.ResourceShutdownListener(){
            @Override
            public void shutdown() {
                BaseClient.this.shutdown();
            }
        });
        try {
            shutdownFinished.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
        }
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }
    @SuppressWarnings("unchecked")
    public void setSubscribeListener(SubscribeListener subscribeListener) {
        this.futureStore.setSubscribeListener(subscribeListener);
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerAddress() {
        return serverHost + ":" + serverPort;
    }
    public int getTimeoutMs() {
        return timeoutMs;
    }

    protected ResultFutureStore getFutureStore() {
        return futureStore;
    }

    public int getIdleReadTime() {
        return idleReadTime;
    }
    public int getIdleWriteTime() {
        return idleWriteTime;
    }

    protected Channel getOriginChannel() {
        return originChannel;
    }

    public boolean isLive() {
        return connected.get() && originChannel != null && originChannel.isOpen();
    }
    public boolean isOpen() {
        return originChannel != null && originChannel.isOpen();
    }
    public void onDisconnect() {
        if(!shutdown.get()) {
            logger.warn("服务端断开连接了: {}", originChannel);
            shutdown();
        }
    }
    public void onReadTimeout() {
        logger.info("读超时响应失败: {}", originChannel);
        if(!isOpen()) {
            try {
                connect(3);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
        }
    }
    public void onWriteTimeout() {
        if(isOpen()) {
            invoke(ExchangeProtocol.ping(System.currentTimeMillis())).listener(new ResultFutureListener() {
                @Override
                public void onSuccess(ExchangeProtocol v) {
                    logger.debug("写超时响应成功: {}", v.getStatus());
                }
                @Override
                public void onFailed(Throwable e) {
                    logger.warn("写超时响应失败: {}", e.getMessage());
                    originChannel.close();
                }
            });
        } else {
            logger.warn("写超时响应失败, 连接已关闭: {}", originChannel);
            originChannel.close();
        }
    }
    @Override
    public void onIdleEvent(ChannelHandlerContext ctx, IdleStateEvent e) {
        if(e == null) {
            onDisconnect();
            return;
        }
        if (e.state() == IdleState.READER_IDLE) {
            onReadTimeout();
        } else if (e.state() == IdleState.WRITER_IDLE) {
            onWriteTimeout();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseClient other = (BaseClient) o;
        return getServerPort() == other.getServerPort()
                && Objects.equals(getServerHost(), other.getServerHost());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getServerHost(), getServerPort());
    }
}