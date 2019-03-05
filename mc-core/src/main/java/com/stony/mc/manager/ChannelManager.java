package com.stony.mc.manager;

import com.stony.mc.NetUtils;
import com.stony.mc.Utils;
import com.stony.mc.concurrent.ConcurrentHashSet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private ChannelManager() {
        this.contextMap = new ConcurrentHashMap<>(1024);
        this.serverInfoMap = new ConcurrentHashMap<>(1024);
        this.subscribeSet = new ConcurrentHashSet<Subscribe>(1024);
        this.deviceRegisterSet = new ConcurrentHashSet<RegisterInfoHolder>(1024);
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

}