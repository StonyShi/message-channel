package com.stony.mc.session;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.BalanceListener;
import com.stony.mc.listener.BusinessHandler;
import com.stony.mc.listener.ComposeHandler;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.manager.ChannelContextHolder;
import com.stony.mc.manager.ChatSession;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.manager.RegisterInfoHolder;
import com.stony.mc.metrics.MetricStatistics;
import com.stony.mc.protocol.*;
import io.netty.channel.ChannelHandlerContext;

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
public class WorkerServer extends BaseServer<WorkerServer> implements Consumer<RegisterInfoHolder> {

    private long heartbeatMS = 3000L;
    private final MetricStatistics metricStatistics;
    private volatile boolean heartbeatRunning = true;

    private HostPort[] masterHostPorts;
    private boolean balance = false;
    private BaseClient masterLive;
    private int masterReties = 1;
    private final BalanceListener balanceHandler;
    private final BusinessHandler businessHandler;

    IdGenerator idWorker = SimpleIdGenerator.getInstance();

    public WorkerServer(String serverName, int serverPort, String[] masterServers, MetricStatistics metricStatistics) {
        super(serverName, serverPort);
        this.balanceHandler = (BalanceListener) this::doBalance;
        businessHandler = new BusinessHandler() {
            /** worker内处理chat逻辑 */
            @Override
            public boolean support(ExchangeTypeEnum typeEnum) {
                return ExchangeTypeEnum.CHAT == typeEnum;
            }
            @Override
            public ExchangeProtocol handle(ExchangeProtocolContext request) {
                return doHandle(request);
            }
        };
        subscribeListener(businessHandler);
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
        final String serverName = getServerName();
        final int serverPort = getServerPort();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BaseClient heartbeat = new BaseClient(){};
                heartbeat.setSubscribeListener(balanceHandler);
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

    private void doBalance(Integer value) {
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

    private ExchangeProtocol doHandle(ExchangeProtocolContext request) {
        //value, name, key : msg, chatId, status
        String chatId = request.getBody().getName();
        //ChatSession.ChatStatus
        String status = request.getBody().getKey();
        ChatSession chatSession = getChannelManager().getChatSession(chatId);
        HostPort hostPort = new HostPort(getServerName(), getServerPort());
        //value, name, key : hostPort, chatId, status
        ExchangeProtocol masterRequest = ExchangeProtocol.
                create(request.getId()).
                type(request.getType()).
                json(hostPort.toJson(), chatId, status);
        if (chatSession == null) {
            return updateMaster(masterRequest, chatId, request.getCtx());
        }
        //update
        if(chatSession.already()) {
            ChannelContextHolder toChannel = chatSession.toChannel(request.getCtx());
            if(chatSession.consistencyWorker()) {
                //转发给to
                toChannel.getCtx().writeAndFlush(ExchangeProtocol.
                        create(request.getId()).
                        type(request.getType()).
                        json(hostPort.toJson(), chatId, status)
                );
                return ExchangeProtocol.ack(request.getId()); //ack from
            } else {
                return ExchangeProtocol.ack(request.getId()).status(ExchangeStatus.wrap(500, chatSession.toString()));
            }
        } else {
            return updateMaster(masterRequest, chatId, request.getCtx());
        }
    }
    private ExchangeProtocol updateMaster(ExchangeProtocol request, String chatId, ChannelHandlerContext ctx) {
        //value, name, key : hostPort, chatId, status
        ResultFuture resultFuture = this.masterLive.submit(request);
        try {
            ExchangeProtocol response = resultFuture.get(2, TimeUnit.SECONDS);
            //重定向
            if (response.getStatus().getCode() == ExchangeStatus.TEMPORARY_REDIRECT.getCode()) {
                String redirectWorker = response.getBody().getFormatValue();
                return ExchangeProtocol.ack(request.getId())
                        .status(ExchangeStatus.wrap(ExchangeStatus.TEMPORARY_REDIRECT.getCode(), redirectWorker));
            }

            if(!response.getStatus().isOk()) {
                //todo master返回异常处理
            }
            //Master成功后更新到worker
            String worker = response.getBody().getFormatValue();
            ChatSession chatSession = getChannelManager().updateChatSession(chatId, ctx);
            chatSession.updateWorker(worker);
            logger.info("更新聊天会话: {}", chatSession);
            return ExchangeProtocol.ack(request.getId());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return ExchangeProtocol.ack(request.getId())
                    .status(ExchangeStatus.SERVICE_UNAVAILABLE);
        }
    }
}