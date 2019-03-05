package com.stony.mc.session;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.ResourceManger;
import com.stony.mc.Utils;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;
import com.stony.mc.protocol.ExchangeTypeEnum;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午4:07
 * @since 2019/1/21
 */
public class SimpleProducer extends BaseClient {
    private AtomicBoolean shutdown = new AtomicBoolean();
    private AtomicBoolean connected = new AtomicBoolean();

    private int masterReties = 1;
    private int workerReties = 2;
    private HostPort[] masterHostPorts;
    private InnerProducerClient delegateClient;
    IdGenerator idWorker = SimpleIdGenerator.getInstance();

    private final Map<String, HostPort> delegateHostPorts;
    private RegisterInfo registerInfo;

    public SimpleProducer(String[] masterServers) {
        this(DEFAULT_CONNECT_TIMEOUT_MILLIS, masterServers);
    }
    public SimpleProducer(int timeoutMs, String[] masterServers) {
        super(timeoutMs);
        initMasterHostPart(Objects.requireNonNull(masterServers, "master servers must be not null"));
        this.delegateHostPorts = new ConcurrentHashMap<>(64);
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
            logger.info("Producer connect begin.");
            return doConnectWorker();
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
    private void reconnect() throws Exception{
        if(!this.shutdown.get()) {
            if(delegateHostPorts.isEmpty()) {
                //分配在线worker
                doAllocationWorkers();
            }
            initConnectWorker();

            if(this.registerInfo != null) {
                this.register(registerInfo);
            }
        }
    }
    private BaseClient doConnectWorker() throws Exception{
        //分配在线worker
        doAllocationWorkers();

        if(delegateHostPorts.isEmpty()) {
            throw new ConnectException("Connect failed, Allocation Worker list is empty.");
        }
        initConnectWorker();
        return this;
    }
    private void initConnectWorker() throws Exception{
        if(delegateClient == null || !delegateClient.isLive()) {
            for (HostPort hostPort: delegateHostPorts.values()) {
                InnerProducerClient workerClient = new InnerProducerClient(getTimeoutMs())
                        .idleReadTime(getIdleReadTime()).idleWriteTime(getIdleReadTime());
                try {
                    workerClient.connect(hostPort.getHost(), hostPort.getPort());
                    if(workerClient.isLive()) {
                        if(delegateClient != null) {
                            delegateClient.shutdown();
                        }
                        this.delegateClient = workerClient;
                    }
                    break;
                } catch (Exception e) {
                    delegateHostPorts.remove(hostPort.line());
                    workerClient.shutdown();
                }
            }
            if(!delegateClient.isLive()) {
                throw new ConnectException("Connect failed, Worker list maybe empty.");
            }
        }
    }
    private void doAllocationWorkers() {
        BaseClient master = getMasterLive();
        try {
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
                        master.shutdown();
                        master = getMasterLive();
                    }
                } catch (Exception e) {
                    logger.warn("Consumer connect to Master[%s:%d] Error: {}", e);
                    if(!master.isLive()) {
                        master.shutdown();
                        master = getMasterLive();
                    }
                }
            }
        } finally {
            master.shutdown();
        }
    }
    private BaseClient getMasterLive() {
        BaseClient master = new BaseClient(){};
        HostPort masterServer = masterHostPorts[0];
        for (int i = 0, len = masterHostPorts.length; i < len; i++) {
            masterServer = masterHostPorts[i];
            logger.info("Producer connect to Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
            try {
                master.connect(masterServer.getHost(), masterServer.getPort(), masterReties);
                break;
            } catch (Exception e) {
                logger.info("Producer Failed to Connect Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
            }
        }
        if (!master.isLive()) {
            master.shutdown();
            SimpleProducer.this.shutdown();
            throw new RuntimeException(String.format("Producer connect to Master[%s:%d] Failed.",
                    masterServer.getHost(), masterServer.getPort()));
        }
        return master;
    }

    @Override
    public boolean isLive() {
        return connected.get() && delegateClient.isLive();
    }
    @Override
    public void shutdown() {
        if(this.shutdown.compareAndSet(false, true)) {
            delegateHostPorts.clear();
            if(delegateClient != null) {
                delegateClient.shutdown();
            }
        }
    }
    final CountDownLatch shutdownFinished = new CountDownLatch(1);
    @Override
    public void waitTillShutdown() {
        ResourceManger.register(new ResourceManger.ResourceShutdownListener(){
            @Override
            public void shutdown() {
                SimpleProducer.this.shutdown();
            }
        });
        try {
            shutdownFinished.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
        }
    }
    public ResultFuture register(RegisterInfo info) {
        this.registerInfo = info;
        return delegateClient.submit(
                ExchangeProtocol.create(idWorker.nextId())
                        .type(ExchangeTypeEnum.REGISTER)
                        .json(JSONObject.toJSONString(info), null, null)
        );
    }
    public ResultFuture sendText(String topic, String key, String v) {
        return delegateClient.submit(
                ExchangeProtocol.create(idWorker.nextId())
                        .text(v, topic, key)
        );
    }
    public ResultFuture sendJson(String topic, String key, byte[] v) {
        return delegateClient.submit(
                ExchangeProtocol.create(idWorker.nextId())
                        .json(v, topic, key)
        );
    }
    public ResultFuture sendJson(String topic, String key, String v) {
        return sendJson(topic, key, v.getBytes(StandardCharsets.UTF_8));
    }
    public ResultFuture sendJson(String topic, String key, Object v) {
        if (String.class.isInstance(v)) {
            return sendJson(topic, key, ((String)v));
        } else if (StringBuilder.class.isInstance(v)) {
            return sendJson(topic, key, v.toString());
        } else if (StringBuffer.class.isInstance(v)) {
            return sendJson(topic, key, v.toString());
        } else if (byte[].class.isInstance(v)) {
            return sendJson(topic, key, (byte[])v);
        } else {
            return sendJson(topic, key, JSONObject.toJSONString(v));
        }
    }


    public void setIdWorker(IdGenerator idWorker) {
        this.idWorker = idWorker;
    }

    class InnerProducerClient extends BaseClient<InnerProducerClient>{
        public InnerProducerClient(int timeoutMs) {
            super(timeoutMs);
        }

        @Override
        public void onDisconnect() {
            super.onDisconnect();
            delegateHostPorts.remove(getServerAddress());
            try {
                reconnect();
            } catch (Exception e) {
                SimpleProducer.this.shutdown();
                throw new RuntimeException(e);
            }
        }
    }
}