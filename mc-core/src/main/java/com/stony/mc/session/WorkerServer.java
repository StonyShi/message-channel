package com.stony.mc.session;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.BalanceListener;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.manager.RegisterInfoHolder;
import com.stony.mc.metrics.MetricStatistics;
import com.stony.mc.protocol.ExchangeName;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;
import com.stony.mc.protocol.ExchangeTypeEnum;

import java.io.Closeable;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午5:49
 * @since 2019/1/15
 */
public class WorkerServer extends BaseServer<WorkerServer> implements BalanceListener, Consumer<RegisterInfoHolder> {

    private long heartbeatMS = 3000L;
    private final MetricStatistics metricStatistics;
    private volatile boolean heartbeatRunning = true;

    private HostPort[] masterHostPorts;
    private boolean balance = false;
    private BaseClient masterLive;
    private int masterReties = 1;

    IdGenerator idWorker = SimpleIdGenerator.getInstance();

    public WorkerServer(String serverName, int serverPort, String[] masterServers, MetricStatistics metricStatistics) {
        super(serverName, serverPort);
        this.metricStatistics = Objects.requireNonNull(metricStatistics, "metricStatistics must be not null");
        setMetricStatistics(metricStatistics);
        initMasterHostPart(Objects.requireNonNull(masterServers, "master servers must be not null"));
    }

    public WorkerServer(int serverPort, String[] masterServers, MetricStatistics metricStatistics) {
        this(null, serverPort, masterServers, metricStatistics);
    }
    public WorkerServer(int serverPort, String[] masterServers){
        this(serverPort, masterServers, new MetricStatistics());
    }

    void initMasterHostPart(String[] masterServers) {
        masterHostPorts = new HostPort[masterServers.length];
        for (int i = 0; i < masterServers.length; i++) {
            String[] master = masterServers[i].split(":");
            masterHostPorts[i] = new HostPort(master[0], Integer.valueOf(master[1]));
        }
    }
    @Override
    public Closeable startup() throws Exception {
        try {
            return super.startup();
        } finally {
            if(isLive()) {
                getChannelManager().setDeviceRegisterCallback(this);
                startHeartbeatThread();
            }
        }
    }
    private void startHeartbeatThread() {
        final SubscribeListener subscribeListener = this;
        final String serverName = getServerName();
        final int serverPort = getServerPort();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BaseClient heartbeat = new BaseClient(){};
                heartbeat.setSubscribeListener(subscribeListener);
                beat_loop:
                while (heartbeatRunning) {
                    HostPort masterServer = masterHostPorts[0];
                    for (int i = 0, len = masterHostPorts.length; i < len; i++) {
                        masterServer = masterHostPorts[i];
                        logger.info("Heartbeat connect to Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
                        try {
                            heartbeat.connect(masterServer.getHost(), masterServer.getPort(), 3);
                            break;
                        } catch (Exception e) {
                            logger.info("Heartbeat Failed to Connect Master[{}:{}].", masterServer.getHost(), masterServer.getPort());
                        }
                    }
                    if(!heartbeat.isLive()) {
                        heartbeat.shutdown();
                        WorkerServer.this.shutdown();
                        throw new RuntimeException(String.format("Heartbeat connect to Master[%s:%d] Failed.",
                                masterServer.getHost(), masterServer.getPort()));
                    }
                    WorkerServer.this.masterLive = heartbeat;
                    logger.info(String.format("Heartbeat connect to Master[%s:%d] Succeed.", masterServer.getHost(), masterServer.getPort()));
                    while (heartbeat.isLive()) {
                        inner_loop: for (int i = 0; i < 3; i++) {
                            ResultFuture future = heartbeat.submit(
                                    ExchangeProtocol
                                            .ping(System.currentTimeMillis())
                                            .json(metricStatistics.getStatisticsJson(serverName, serverPort),
                                                    ExchangeName.HEARTBEAT, null)
                            );
                            try {
                                ExchangeProtocol value = future.get(heartbeatMS, TimeUnit.MILLISECONDS);
                                if(value.getStatus() != ExchangeStatus.OK) {
                                    logger.warn("Heartbeat Ack: {}", value.getStatus());
                                }
                            } catch (TimeoutException te) {
                                logger.warn(String.format("Heartbeat connect to Master[%s:%d] Timeout.", masterServer.getHost(), masterServer.getPort()));
                                if(!heartbeat.isLive()) {
                                    continue beat_loop;
                                }
                                continue inner_loop;
                            } catch (Exception e) {
                                logger.warn("Heartbeat connect to Master[%s:%d] Error: {}", e);
                                continue beat_loop;
                            }
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(heartbeatMS);
                        } catch (InterruptedException e) {}
                    }
                }
            }
        });
        t.setName("worker-heartbeat");
        t.setDaemon(true);
        t.start();
    }
    @Override
    public void shutdown() {
        try {
            this.heartbeatRunning = false;
        } finally {
            super.shutdown();
        }
    }

    public WorkerServer heartbeat(long heartbeatMS) {
        this.heartbeatMS = heartbeatMS;
        return this;
    }
    public WorkerServer heartbeat(Duration duration) {
        this.heartbeatMS = duration.toMillis();
        return this;
    }

    @Override
    public void onBalance(Integer value) {
        if(balance) {
            getChannelManager().balanceServer(value);
        }
    }

    public void setBalance(boolean balance) {
        this.balance = balance;
    }

    public HostPort[] getMasterHostPorts() {
        return masterHostPorts;
    }

    @Override
    public void accept(RegisterInfoHolder registerInfoHolder) {
        if(this.masterLive.isLive()) {
            RegisterInfo info = registerInfoHolder.getInfo();
            info.setWorkerHost(getServerName());
            info.setWorkerPort(getServerPort());
            getExecutor().submit(()-> {
                this.masterLive.execute(
                        ExchangeProtocol.create(idWorker.nextId())
                                .type(ExchangeTypeEnum.REGISTER)
                                .json(JSONObject.toJSONString(info), null, null)
                );
            });
        }
    }
}