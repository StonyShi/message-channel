package com.stony.mc.manager;

import com.stony.mc.NetUtils;
import com.stony.mc.Utils;
import com.stony.mc.concurrent.ConcurrentHashSet;
import com.stony.mc.session.HostPort;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午3:00
 * @since 2019/1/8
 */
public class ChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    private final Map<ChannelHandlerContext, ChannelContextHolder> contextMap;
    private final Map<String, ServerInfo> serverInfoMap;
    private final Set<Subscribe> subscribeSet;
    private final Set<RegisterInfoHolder> deviceRegisterSet;
    private Consumer<RegisterInfoHolder> deviceRegisterCallback;
    private final Map<String, ChatSession> chatSessionMap;
    private int serverPort;
    private String serverName;

    private ChannelManager() {
        this.contextMap = new ConcurrentHashMap<>(1024);
        this.serverInfoMap = new ConcurrentHashMap<>(1024);
        this.subscribeSet = new ConcurrentHashSet<Subscribe>(1024);
        this.deviceRegisterSet = new ConcurrentHashSet<RegisterInfoHolder>(1024);
        this.chatSessionMap = new ConcurrentHashMap<String, ChatSession>(1024);
    }

    private final Lock chatSessionLock = new ReentrantLock();
    public ChatSession getChatSession(String chatId) {
        return chatSessionMap.get(chatId);
    }
    public ChatSession createChatSession(String chatId, String worker) {
        ChatSession chatSession = chatSessionMap.get(chatId);
        if(chatSession == null) {
            chatSessionLock.lock();
            try {
                chatSession = chatSessionMap.get(chatId);
                if(chatSession == null) {
                    chatSession = new ChatSession();
                    chatSession.setChatId(chatId);
                    chatSession.setLeaderWorker(worker);
                    chatSessionMap.put(chatId, chatSession);
                    return chatSession;
                }
            } finally {
                chatSessionLock.unlock();
            }
        }
        return chatSession;
    }
    /** worker 需要保存上下文 **/
    public ChatSession updateChatSession(String chatId, ChannelHandlerContext ctx) {
        return updateChatSession(chatId, ctx, null);
    }

    /**
     *
     * @param chatId
     * @param ctx
     * @param workerHostPort  null      Worker需要保存上下文holder, holder是客户端,
     *                        notnull   Master不需要保存上下文holder， holder是Worker
     * @return
     */
    public ChatSession updateChatSession(String chatId, ChannelHandlerContext ctx, HostPort workerHostPort) {
        ChatSession chatSession = chatSessionMap.get(chatId);
        boolean caveContext = (workerHostPort == null);
        //TODO 保存会话逻辑、断线重连优化，master会话信息持久化，空闲超时、过期销毁
        if(chatSession == null) {
            //新建会话
            chatSession = new ChatSession();
            chatSession.setChatId(chatId);
            ChannelContextHolder holder = new ChannelContextHolder(ctx);
            //保存channel上下文
            if(caveContext) {
                chatSession.setLeader(holder);
                logger.info(String.format("Worker创建聊天会话：cid=%s, leader=%s",
                        chatId,
                        holder.getAddress()));
            } else {
                if(Utils.isEmpty(workerHostPort.getHost())) {
                    workerHostPort.setHost(holder.getHostPort().getHost());
                }
                logger.info(String.format("Master创建聊天会话：cid=%s, leaderWorker=%s",
                        chatId,
                        workerHostPort.line()));
                chatSession.setLeaderWorker(workerHostPort.line());
            }
            chatSession.setCreateLeader(true);
            chatSessionMap.put(chatId, chatSession);
            return chatSession;
        }
        //分布在同一台Worker
        if(chatSession.consistencyWorker()) {
            return chatSession;
        }
        //更新会话
        ChannelContextHolder holder = new ChannelContextHolder(ctx);
        if (workerHostPort == null) {
            logger.info(String.format("Worker更新聊天会话：cid=%s, leader=%s, follower=%s",
                    chatId,
                    chatSession.getLeaderAddress(),
                    holder.getAddress()));
            chatSession.setFollower(holder);
        } else {
            if(Utils.isEmpty(workerHostPort.getHost())) {
                workerHostPort.setHost(holder.getHostPort().getHost());
            }
            logger.info(String.format("Master更新聊天会话：cid=%s, leaderWorker=%s, followerWorker=%s,",
                    chatId,
                    chatSession.getLeaderWorker(),
                    workerHostPort.line()));
            chatSession.setFollowerWorker(workerHostPort.line());
        }
        chatSession.setUpdateTime(System.currentTimeMillis());
        chatSession.setCreateLeader(false);
        return chatSession;
    }
    public boolean deviceRegister(RegisterInfoHolder info) {
        if(Utils.isEmpty(info.getInfo().getAddress())) {
            info.getInfo().setAddress(NetUtils.getChannelHostPort(info.getCtx()).line());
        }
        logger.info(String.format("注册设备：uid=%s, device=%s, address=%s",
                info.getInfo().getUid(),
                info.getInfo().getDevice(),
                info.getInfo().getAddress()));
        try {
            return deviceRegisterSet.add(info);
        } finally {
            if(deviceRegisterCallback != null) {
                deviceRegisterCallback.accept(info);
            }
        }
    }
    public List<RegisterInfoHolder> deviceLiveList() {
        return deviceRegisterSet.stream().filter(v -> v.isLive()).collect(Collectors.toList());
    }
    public void register(ChannelHandlerContext ctx) {
        contextMap.put(ctx, new ChannelContextHolder(ctx));
        logger.info("注册连接：{}", ctx.channel().remoteAddress());
    }
    public void unregister(ChannelHandlerContext ctx) {
        //un subscribe
        Subscribe[] sbs = subscribeSet.stream().filter(sb -> sb.getCtx().equals(ctx)).toArray(Subscribe[]::new);
        for(Subscribe sb : sbs) {
            unsubscribe(sb);
        }
        logger.info("注销连接：{}", ctx.channel().remoteAddress());
        contextMap.remove(ctx);
    }
    public boolean subscribe(Subscribe subscribe) {
        logger.info("开始订阅：[{} -> {} -> {} -> {}]",
                subscribe.getCtx().channel().remoteAddress(),
                subscribe.getGroup(),
                subscribe.getTopic(),
                subscribe.getKey()
        );
        ChannelContextHolder holder = contextMap.get(subscribe.getCtx());
        if (holder != null) {
            holder.subscribe();
        }
        return subscribeSet.add(subscribe);
    }
    public boolean unsubscribe(Subscribe subscribe) {
        logger.info("取消订阅：[{} -> {} -> {} -> {}]",
                subscribe.getCtx().channel().remoteAddress(),
                subscribe.getGroup(),
                subscribe.getTopic(),
                subscribe.getKey()
        );
        ChannelContextHolder holder = contextMap.get(subscribe.getCtx());
        if (holder != null) {
            holder.unsubscribe();
        }
        return subscribeSet.remove(subscribe);
    }
    public List<Subscribe> getSubscriptionList(final String topic, final String key) {
        List<Subscribe> list =  subscribeSet.stream().filter(sb -> {
            if(sb.getKey() == null || sb.getKey().equals("*")) {
                return sb.getTopic().equals(topic);
            } else {
                return sb.getTopic().equals(topic) && sb.getKey().equals(key);
            }
        }).collect(Collectors.toList());
        logger.debug("获取订阅列表: {} <> {} >>> {}", topic, key, list);
        return list;
    }

    public static ChannelManager create() {
        return new ChannelManager();
    }

    public Set<ChannelHandlerContext> getLiveContextList() {
        return contextMap.keySet();
    }

    public void shutdown() {
        try {
            contextMap.keySet()
                    .stream()
                    .map(ChannelHandlerContext::close)
                    .collect(Collectors.partitioningBy(Future::isSuccess))
                    .entrySet().forEach(kv -> {
                if(kv.getKey()) {
                    System.out.println(String.format("关闭注册连接成功：%s 个", kv.getValue().size()));
                } else {
                    System.out.println(String.format("关闭注册连接失败：%s 个", kv.getValue().size()));
                }
            });
        } finally {
            contextMap.clear();
            subscribeSet.clear();
        }
    }
    int serverLiveTimeMS = 20000;
    public String allocatedServer(boolean all) {
        if(all) {
            return serverInfoMap.values()
                    .stream()
                    .filter(info -> (System.currentTimeMillis() - info.getCreatedTime() < serverLiveTimeMS))
                    .sorted(ServerInfo.getComparator())
                    .map(ServerInfo::getAddress)
                    .collect(Collectors.joining(","));
        }
        Optional<ServerInfo> value = serverInfoMap.values()
                .stream()
                .filter(info -> (System.currentTimeMillis() - info.getCreatedTime() < serverLiveTimeMS))
                .sorted(ServerInfo.getComparator())
                .findFirst();
        if (!value.isPresent()) {
            logger.trace("allocated server: {}", value);
            return value.get().getAddress();
        }
        return null;
    }
    public void balanceServer(int avg) {
        Collection<ChannelContextHolder> values = contextMap.values();
        ChannelContextHolder[] contextHolders = values.toArray(new ChannelContextHolder[values.size()]);
        Arrays.sort(contextHolders, new Comparator<ChannelContextHolder>() {
            @Override
            public int compare(ChannelContextHolder o1, ChannelContextHolder o2) {
                return o1.getLastProcessingTime().compareTo(o2.getLastProcessingTime());
            }
        });

        logger.debug(String.format("Server begin balance: %s >>> %s", contextHolders.length, avg));
        int count = contextHolders.length - avg;
        if (count > 0) {
            for (ChannelContextHolder holder : contextHolders) {
                if (!holder.isProcessing() && !holder.isSubscribe()) {
                    holder.getCtx().close();
                    count--;
                }
                if (count < 0) {
                    break;
                }
            }
        }
    }
    public void processing(ChannelHandlerContext ctx, boolean process){
        ChannelContextHolder holder = contextMap.get(ctx);
        if(holder != null) {
            if (process) {
                holder.beginProcessing();
            } else {
                holder.endProcessing();
            }
        }
    }
    public long updateServerInfo(ServerInfo serverInfo) {
        serverInfoMap.put(serverInfo.getAddress(), serverInfo);
        double avg = serverInfoMap.values().stream().mapToLong(v -> v.getConnectionCount()).average().orElse(0);
        long useAvg = (long) avg;
        if(serverInfo.getConnectionCount() > useAvg) {
            return useAvg;
        }
        return 0;
    }

    public void setDeviceRegisterCallback(Consumer<RegisterInfoHolder> deviceRegisterCallback) {
        this.deviceRegisterCallback = deviceRegisterCallback;
    }

    public boolean hasSubscribe() {
        return !subscribeSet.isEmpty();
    }

    public boolean hasLive() {
        return !contextMap.isEmpty();
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }
    public String getServerName() {
        return serverName;
    }

}