package com.stony.mc.session;

import com.stony.mc.ResourceManger;
import com.stony.mc.Utils;
import com.stony.mc.concurrent.NamedThreadFactory;
import com.stony.mc.concurrent.TaskExecutorFactory;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.*;
import com.stony.mc.manager.SubscribeInfo;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;
import com.stony.mc.protocol.ExchangeTypeEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 上午9:55
 * @since 2019/1/18
 */
public class SimpleConsumer extends BaseClient implements SubscribeListener {

    private AtomicBoolean shutdown = new AtomicBoolean();
    private AtomicBoolean connected = new AtomicBoolean();

    private HostPort[] masterHostPorts;
    private Map<String, InnerConsumerClient> delegateClients;
    private final Map<String, HostPort> delegateHostPorts;
    private final SubscriberProcessor subscriberProcessor;
    int masterReties = 1;
    int workerReties = 2;
    static int DEFAULT_IDLE_READ_TIME = 120;
    int delayTime = 5; //秒
    SubscribeInfo subscribeInfo;
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("consumer-scheduled-"));

    IdGenerator idWorker = SimpleIdGenerator.getInstance();
    BaseClient masterLive;

    @Override
    public boolean support(ExchangeTypeEnum typeEnum) {
        return ExchangeTypeEnum.MESSAGE == typeEnum;
    }

    public SimpleConsumer(String[] masterServers) {
        this(DEFAULT_CONNECT_TIMEOUT_MILLIS, masterServers);
    }
    public SimpleConsumer(int timeoutMs, String[] masterServers) {
        super(timeoutMs);
        initMasterHostPart(Objects.requireNonNull(masterServers, "master servers must be not null"));
        this.delegateHostPorts = new ConcurrentHashMap<>(64);
        this.delegateClients = new ConcurrentHashMap<>(64);
        this.subscriberProcessor = new SubscriberProcessor(TaskExecutorFactory.getFixedExecutor("consumer", 1, 2000));
        setIdleReadTime(DEFAULT_IDLE_READ_TIME);
    }


    void initMasterHostPart(String[] masterServers) {
        masterHostPorts = new HostPort[masterServers.length];
        for (int i = 0; i < masterServers.length; i++) {
            String[] master = masterServers[i].split(":");
            masterHostPorts[i] = new HostPort(master[0], Integer.valueOf(master[1]));
        }
    }

    @Override
    public BaseClient connect() throws Exception {
        if(this.connected.compareAndSet(false, true)) {
            logger.info("Consumer connect begin.");
            checkWorkerChange();
            return doConnectWorkers(false);
        }
        return this;
    }

    @Override
    public BaseClient connect(int masterReties) throws Exception {
        this.masterReties = masterReties;
        return this.connect();
    }

    @Override
    public BaseClient connect(String serverHost, int serverPort) throws Exception {
        return this.connect();
    }

    public void setWorkerReties(int workerReties) {
        this.workerReties = workerReties;
    }

    public void setIdWorker(IdGenerator idWorker) {
        this.idWorker = idWorker;
    }

    private BaseClient doConnectWorkers(boolean subscribe) throws Exception{
        try {
            final SubscribeListener subscribeListener = this;
            //分配在线worker
            doAllocationWorkers();

            if(delegateHostPorts.isEmpty()) {
                throw new RuntimeException("Worker list is empty.");
            }
//            logger.info("Consumer allocation worker list: {}", delegateHostPorts.values());
            for (HostPort hostPort: delegateHostPorts.values()) {
                //创建不存在的workerClient
                if(!delegateClients.containsKey(hostPort.line())) {
                    InnerConsumerClient workerClient = new InnerConsumerClient(getTimeoutMs())
                            .idleReadTime(getIdleReadTime()).idleWriteTime(getIdleReadTime());
                    delegateClients.put(hostPort.line(), workerClient);
                    try {
                        workerClient.setSubscribeListener(subscribeListener);
                        workerClient.connect(hostPort.getHost(), hostPort.getPort(), workerReties);
                        if(subscribe && subscribeInfo != null) {
                            workerClient.doSubscribe(idWorker.nextId(), subscribeInfo).get(6, TimeUnit.SECONDS);
                            workerClient.setReconnecting(false);
                        }
                    } catch (Exception e) {
                        logger.info("connection worker[{}] error: {}", hostPort, e);
                        workerClient.shutdown();
                    }
                }
            }
            return this;
        } catch (Exception e) {
            throw e;
        }
    }
    private void doAllocationWorkers() throws Exception {
        BaseClient master = checkMasterLive();
        //retry_allocation:
        for (int i = 0; i < 3; i++) {
            ResultFuture future = master.submit(ExchangeProtocol.allocation(idWorker.nextId(), true));
            try {
                ExchangeProtocol value = future.get(3000, TimeUnit.MILLISECONDS);
                if(value.getStatus() == ExchangeStatus.OK) {
                    if(value.getBody() != null || Utils.isNotEmpty(value.getBody().getValue())) {
                        String worker = new String(value.getBody().getValue(), StandardCharsets.UTF_8);
                        if(Utils.isNotEmpty(worker)) {
                            boolean change = false;
                            String[] workers = worker.split(",");
                            for(String s : workers) {
                                String[] vs = s.split(":");
                                HostPort hp = HostPort.wrap(vs[0], Integer.valueOf(vs[1]));
                                if (!delegateHostPorts.containsKey(hp.line())) {
                                    delegateHostPorts.put(hp.line(), hp);
                                    change = true;
                                }
                            }
                            if(change) {
                                logger.info(String.format("Allocation: [%s]", worker));
                            }
                            break;
                        }
                    }
                } else {
                    logger.warn("Consumer allocation Ack: {}", value.getStatus());
                }
            } catch (TimeoutException te) {
                logger.warn(String.format("Consumer connect to Master[%s] Timeout.",
                        master.getServerAddress()));
                if(!master.isLive()) {
                    master = checkMasterLive();
                }
            } catch (Exception e) {
                logger.warn("Consumer connect to Master[%s:%d] Error: {}", e);
                if(!master.isLive()) {
                    master = checkMasterLive();
                }
            }
        }
    }
    private BaseClient checkMasterLive() throws Exception {
        if(this.masterLive != null && this.masterLive.isLive()) {
            return this.masterLive;
        }
        BaseClient master = new BaseClient(){};
        HostPort masterServer = (masterLive == null) ?
                masterHostPorts[0] :
                HostPort.wrap(masterLive.getServerHost(), masterLive.getServerPort());
        for (int i = 0, len = masterHostPorts.length; i < len; i++) {
            masterServer = masterHostPorts[i];
            logger.info("Consumer connect to Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
            try {
                master.connect(masterServer.getHost(), masterServer.getPort(), masterReties);
                break;
            } catch (Exception e) {
                logger.info("Consumer Failed to Connect Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
            }
        }
        if(!master.isLive()) {
            master.shutdown();
            SimpleConsumer.this.shutdown();
            throw new ConnectException(String.format("Consumer connect to Master[%s:%d] Failed.",
                    masterServer.getHost(), masterServer.getPort()));
        }
        if(this.masterLive != null) {
            this.masterLive.shutdown();
        }
        this.masterLive = master;
        return this.masterLive;
    }

    @Override
    public boolean isLive() {
        return connected.get();
    }
    @Override
    public void shutdown() {
        if(this.shutdown.compareAndSet(false, true)) {
            try {
                scheduler.shutdown();
                this.masterLive.shutdown();
            } finally {
                for (InnerConsumerClient worker: delegateClients.values()) {
                    worker.shutdown();
                }
                shutdownFinished.countDown();
            }
        }
    }
    final CountDownLatch shutdownFinished = new CountDownLatch(1);
    @Override
    public void waitTillShutdown() {
        ResourceManger.register(new ResourceManger.ResourceShutdownListener(){
            @Override
            public void shutdown() {
                SimpleConsumer.this.shutdown();
            }
        });
        try {
            this.shutdownFinished.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
        }
    }

    public void subscribe(String group, String topic, String key, TopicRecordListener listener) {
        Objects.requireNonNull(group, "group must be not null.");
        Objects.requireNonNull(topic, "topic must be not null.");
        Objects.requireNonNull(listener, "listener must be not null.");
        if(!connected.get()) {
            throw new RuntimeException("Consumer not connected.");
        }

        this.subscribeInfo = new SubscribeInfo(group, topic, key);

        for (InnerConsumerClient worker: delegateClients.values()) {
            if(worker.isLive()) {
                ResultFuture future = worker.doSubscribe(idWorker.nextId(), subscribeInfo);
                try {
                    ExchangeProtocol v = future.get(6, TimeUnit.SECONDS);
                    if(v.getStatus().isOk()) {
                        logger.info("subscribe to {} succeed.", worker.getServerAddress());
                    } else {
                        logger.warn("subscribe to {} failed.", worker.getServerAddress());
                    }
                } catch (Exception e) {
                    logger.error("subscribe to {} error.", worker.getServerAddress(), e);
                }
            } else {
                logger.warn("subscribe to {} not live.", worker.getServerAddress());
            }
        }
        subscriberProcessor.addListener(listener);
    }

    @Override
    public void onSubscribe(ExchangeProtocol value) {
        subscriberProcessor.onSubscribe(value);
    }
    private void checkWorkerChange() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                doConnectWorkers(true);
                for (InnerConsumerClient worker : delegateClients.values()) {
                    if (!worker.isLive() && !worker.isReconnecting()) {
                        doDelayConnection(new DelayConnectionClient(worker, delayTime, 3));
                    }
                }
            } catch (Exception e) {
               logger.error("校验WorkerClient异常: ",  e);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    private void doDelayConnection(final DelayConnectionClient delayConnectionClient) {
        scheduler.schedule(() -> {
            //重连&订阅
            if(!delayConnectionClient.reconnection()) {
                if(!delayConnectionClient.isDone()) {
                    doDelayConnection(delayConnectionClient);
                } else {
                    delegateHostPorts.remove(delayConnectionClient.line());
                    InnerConsumerClient dc = delegateClients.remove(delayConnectionClient.line());
                    if (dc != null) {
                        dc.shutdown();
                    }
                }
            }
        }, delayConnectionClient.getNextDelayTime(), TimeUnit.SECONDS);
    }
    class InnerConsumerClient extends BaseClient<InnerConsumerClient> {
        volatile boolean reconnecting = false;
        public InnerConsumerClient(int timeoutMs) {
            super(timeoutMs);
        }
        public ResultFuture doSubscribe(long id, SubscribeInfo subscribeInfo) {
            return submit(ExchangeProtocol.subscribe(id,
                    subscribeInfo.getGroup(),
                    subscribeInfo.getTopic(),
                    subscribeInfo.getKey()));
        }
        public void reconnect() throws Exception {
            if(!isLive()) {
                connect();
                ExchangeProtocol v = doSubscribe(idWorker.nextId(), subscribeInfo).get(6, TimeUnit.SECONDS);
                if(!v.getStatus().isOk()) {
                    throw new RuntimeException(String.format("订阅[%s - %s - %s] 失败 %s",
                            subscribeInfo.getGroup(),
                            subscribeInfo.getTopic(),
                            subscribeInfo.getKey(),
                            getServerAddress())
                    );
                }
            }
        }
        @Override
        public void onReadTimeout() {
            if(!isLive()) {
                logger.info("服务[{}]读超时, 响应失败.", this.getServerAddress());
                doDelayConnection(new DelayConnectionClient(this, delayTime));
            } else {
                final InnerConsumerClient client =  this;
                execute(ExchangeProtocol.ping(idWorker.nextId())).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()) {
                        } else {
                            doDelayConnection(new DelayConnectionClient(client, delayTime));
                        }
                    }
                });
            }
        }
        @Override
        public void onDisconnect() {
            logger.warn("Worker[{}]断开连接了, 重连一次", this.getServerAddress());
            if(!isLive()) {
                try {
                    reconnect();
                } catch (Exception e) {
                    logger.warn("Worker[{}]重连失败, 延迟{}秒重试,", this.getServerAddress(), delayTime);
                    doDelayConnection(new DelayConnectionClient(this, delayTime));
                }
            }
        }
        public void setReconnecting(boolean reconnecting) {
            this.reconnecting = reconnecting;
        }
        public boolean isReconnecting() {
            return reconnecting;
        }
    }
    class DelayConnectionClient {
        InnerConsumerClient client;
        int delayTime;
        int delayCount = 1;
        int delayMaxCount = 1 + 2; //reties 2
        public DelayConnectionClient(InnerConsumerClient client, int delayTime, int delayMaxCount) {
            this(client, delayTime);
            this.delayMaxCount = Math.max(2, delayMaxCount);
        }
        public DelayConnectionClient(InnerConsumerClient client, int delayTime) {
            this.client = client;
            this.delayTime = delayTime;
            this.client.setReconnecting(true);
        }
        public boolean reconnection(){
            boolean connected = true;
            try {
                logger.info("Worker[{}]延迟{}秒, 重连{}次", client.getServerAddress(), delayTime, delayCount);
                delayCount++;
                delayTime = delayTime * delayCount;
                client.reconnect();
            } catch (Exception e) {
                connected = false;
            }
            return connected && client.isLive();
        }
        public int getNextDelayTime() {
            return delayTime;
        }
        public boolean isDone() {
            boolean done = delayCount >= delayMaxCount;
            if(done) {
                this.client.setReconnecting(false);
            }
            return done;
        }
        public String line() {
            return client.getServerHost() + ":" + client.getServerPort();
        }
    }
}