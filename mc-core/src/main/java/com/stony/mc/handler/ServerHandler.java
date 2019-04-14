package com.stony.mc.handler;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.ClockUtils;
import com.stony.mc.Utils;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.manager.*;
import com.stony.mc.metrics.EmptyMetricEventListener;
import com.stony.mc.metrics.MetricEvent;
import com.stony.mc.metrics.MetricEventListener;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeResponseConsumer;
import com.stony.mc.protocol.ExchangeStatus;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *<pre>
 *  {@link io.netty.channel.ChannelInboundHandler}
 *  {@link io.netty.channel.ChannelInboundHandlerAdapter}
 *</pre>
 * @author stony
 * @version 上午10:46
 * @since 2019/1/3
 */
public class ServerHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    final ChannelManager channelManager;
    final ThreadPoolExecutor executor;
    final SubscribeListener businessHandler;
    final MetricEventListener metricEventListener;

    public ServerHandler(ChannelManager channelManager, ThreadPoolExecutor executor) {
        this(channelManager, executor, null);
    }

    public ServerHandler(ChannelManager channelManager, ThreadPoolExecutor executor, SubscribeListener subscribeListener) {
        this(channelManager, executor, subscribeListener, new EmptyMetricEventListener());
    }
    public ServerHandler(ChannelManager channelManager, ThreadPoolExecutor executor, SubscribeListener subscribeListener,  MetricEventListener metricEventListener) {
        this.channelManager = channelManager;
        this.executor = executor;
        this.businessHandler = subscribeListener;
        if ((metricEventListener == null)) {
            this.metricEventListener = new EmptyMetricEventListener();
        } else {
            this.metricEventListener = metricEventListener;
        }
    }
    List<SubscribeHolder> getSubscribes(final ExchangeProtocol value) {
        return channelManager.getSubscriptionList(value.getBody().getName(), value.getBody().getKey())
                .stream()
                .collect(Collectors.groupingBy(Subscribe::getGroup))
                .entrySet()
                .stream()
                .map(kv -> {
                    int size = kv.getValue().size();
                    Subscribe fs = kv.getValue().stream().sorted(Subscribe.getComparator()).findFirst().get();
                    logger.debug("subscriber group[{} -> {}] selected {}", kv.getKey(), size, fs);
                    return fs;
                })
                .filter(sb -> sb != null && sb.getCtx() != null)
                .map(sb -> new SubscribeHolder(sb, value))
                .collect(Collectors.toList());
    }
    List<RegisterInfoHolder> getDeviceList(final ExchangeProtocol value) {
        List<RegisterInfoHolder> live = channelManager.deviceLiveList();
        if(value.getBody() == null || value.getBody().getValue() == null){
            return live;
        }
        RegisterInfoFilter filter = JSONObject.parseObject(value.getBody().getValue(), RegisterInfoFilter.class);
        if(filter.isAllEmpty()) {
            return live;
        }
        return live
                .stream()
                .filter(v -> filter.test(v.getInfo()))
                .collect(Collectors.toList());
    }
    private CompletableFuture<SubscribeHolder> writeAndFlush(SubscribeHolder subscribeHolder) {
        return CompletableFuture.supplyAsync(subscribeHolder::writeAndFlush, executor)
                .thenApply(SubscribeHolder::success)
                .exceptionally(ex -> subscribeHolder.failed());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol value) throws Exception {
        logger.trace("server read: {}", value);
        metricEventListener.onEvent(MetricEvent.REQUEST);
        metricEventListener.onEvent(MetricEvent.REQUEST_BYTE_SIZE, (17L+value.getBodyLen()));
        channelReadValue(ctx, value);
    }
    @SuppressWarnings("unchecked")
    <U> CompletableFuture<List<U>> updateAsync(Supplier<List<U>> supplier, int splitSize, Function<U, U> update, Executor executor) {
        return CompletableFuture.supplyAsync(supplier, executor).thenCompose(list -> {
            if(list.size() > splitSize) {
                CompletableFuture<List<U>>[] updateds = list.stream().collect(Collectors.groupingBy(sb -> sb.hashCode() % splitSize))
                        .entrySet()
                        .stream()
                        .map(kv -> CompletableFuture.supplyAsync(() -> {
                            kv.getValue().forEach(update::apply);
                            return kv.getValue();
                        }, executor))
                        .toArray(CompletableFuture[]::new);
                return CompletableFuture.allOf(updateds).thenApplyAsync(av -> Arrays.stream(updateds)
                        .map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(toList()), executor
                );
            } else {
                return CompletableFuture.completedFuture(
                        list.stream()
                        .map(update)
                        .collect(Collectors.toList())
                );
            }
        });
    }
    @SuppressWarnings("unchecked")
    private void channelReadValue(final ChannelHandlerContext ctx, final ExchangeProtocol value) throws Exception {
        switch (value.getType()) {
            case NOTIFY:
                try {
                    doProcessing(ctx, true);
                    List<RegisterInfoHolder> registerList = getDeviceList(value);
                    if (!registerList.isEmpty()) {
                        updateAsync(() -> registerList
                                        .stream().map(xc -> new ExchangeProtocolHolder(xc.getCtx(), value))
                                        .collect(Collectors.toList()),
                                8,
                                ExchangeProtocolHolder::writeAndFlush,
                                executor)
                                .whenComplete((res, ev) -> {
                                    if (ev == null) {
                                        if (res.isEmpty()) {
                                            doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                                                    .status(ExchangeStatus.wrap(500, "The notify was send is empty")));
                                            logger.debug("The notify was send is empty");
                                        } else {
                                            doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                                            long sum = res.stream().filter(ExchangeProtocolHolder::isSuccess).count();
                                            logger.debug("The notify was send success {} times, failed {} times",
                                                    sum, (res.size() - sum));
                                        }
                                    } else {
                                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                                                .status(ExchangeStatus.wrap(500, String.format("The notify was send error:%s", ev))));
                                        logger.error("The notify[{}] was send error", value, ev);
                                    }
                                });
                    } else {
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).status(ExchangeStatus.wrap(500, "The register device live list is empty")));
                    }
                } finally {
                    doProcessing(ctx, false);
                }
                break;
            case MESSAGE:
                final boolean hasSubscribe = channelManager.hasSubscribe();
                if (value.getBody() != null
                        && (value.getBody().getValue() != null)
                        && hasSubscribe) {
                    try {
                        doProcessing(ctx, true);
                        updateAsync(() -> getSubscribes(value),
                                8,
                                SubscribeHolder::writeAndFlush,
                                executor
                        ).whenComplete((res, ev) -> {
                            if (ev == null) {
                                if (res.isEmpty()) {
                                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                                            .status(ExchangeStatus.wrap(500, "The worker subscriber is empty")));
                                    logger.debug("The worker subscriber is empty");
                                } else {
                                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                                    long sum = res.stream().filter(SubscribeHolder::isSuccess).count();
                                    logger.debug("The message was subscribed success {} times, failed {} times",
                                            sum, (res.size() - sum));
                                }
                            } else {
                                doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                                        .status(ExchangeStatus.wrap(500, String.format("The message was subscribed error:%s", ev))));
                                logger.error("The message[{}] was subscribed error", value, ev);
                            }
                        });
                    } finally {
                        doProcessing(ctx, false);
                    }
//                    CompletableFuture.supplyAsync(() -> getSubscribes(value), elasticExecutor)
//                            .thenCompose(list -> {
//                                if (list.size() > 8) {
//                                    CompletableFuture<List<SubscribeHolder>>[] updateds = list.stream().collect(Collectors.groupingBy(sb -> sb.hashCode() % 8))
//                                            .entrySet()
//                                            .stream()
//                                            .map(kv -> CompletableFuture.supplyAsync(() -> {
//                                                kv.getValue().forEach(SubscribeHolder::writeAndFlush);
//                                                return kv.getValue();
//                                            }, elasticExecutor))
//                                            .toArray(CompletableFuture[]::new);
//                                    return CompletableFuture.allOf(updateds).thenApplyAsync(av -> Arrays.stream(updateds)
//                                            .map(CompletableFuture::join)
//                                            .flatMap(Collection::stream)
//                                            .collect(toList()), elasticExecutor
//                                    );
//                                } else {
//                                    List<SubscribeHolder> upList = list.stream()
//                                            .map(SubscribeHolder::writeAndFlush)
//                                            .collect(Collectors.toList());
//                                    return CompletableFuture.completedFuture(upList);
//                                }
//                            })
//                            .whenComplete((res, ev) -> {
//                                if (ev == null) {
//                                    if (res.isEmpty()) {
//                                        logger.debug("The message was subscribed is empty");
//                                    } else {
//                                        long sum = res.stream().filter(SubscribeHolder::isSuccess).count();
//                                        logger.debug("The message was subscribed success {} times, failed {} times",
//                                                sum, (res.size() - sum));
//                                    }
//                                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
//                                } else {
//                                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
//                                            .status(ExchangeStatus.INTERNAL_SERVER_ERROR));
//                                    logger.error("The message[{}] was subscribed error", value, ev);
//                                }
//                            });
                } else {
                    if (hasSubscribe) {
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).status(ExchangeStatus.BAD_REQUEST));
                    } else {
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).status(ExchangeStatus.wrap(500, "The worker subscriber is empty")));
                    }
                }
                break;
            case SUBSCRIBE:
                JSONObject info = JSONObject.parseObject(value.getBody().getFormatValue());
                if (channelManager.subscribe(new Subscribe(ctx, info.getString("group"), info.getString("topic"), info.getString("key")))) {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                } else {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                            .status(ExchangeStatus.wrap(500, "subscribe failed"))
                    );
                }
                break;
            case UNSUBSCRIBE:
                JSONObject info2 = JSONObject.parseObject(value.getBody().getFormatValue());
                if (channelManager.unsubscribe(new Subscribe(ctx, info2.getString("group"), info2.getString("topic"), info2.getString("key")))) {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                } else {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).
                            status(ExchangeStatus.wrap(500, "unsubscribe failed"))
                    );
                }
                break;
            case ALLOCATION:
                boolean all = false;
                if (value.getBody() != null && value.getBody().getValue() != null) {
                    all = Boolean.valueOf(value.getBody().getFormatValue());
                }
                String allocatedServer = channelManager.allocatedServer(all);
                if(Utils.isEmpty(allocatedServer)) {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).
                            status(ExchangeStatus.wrap(500, "not found server"))
                    );
                    logger.warn("allocated server failed to {}", ctx.channel().remoteAddress());
                } else {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                            .text(allocatedServer, null, null));
                    logger.debug("allocated server {} to {}", allocatedServer, ctx.channel().remoteAddress());
                }
                break;
            case CHAT:
                //worker 自定义处理
                if (supportBusiness(value)) {
                    doCustomHandle(new ExchangeResponseConsumer(value, ctx) {
                        @Override
                        public void accept(ChannelHandlerContext curCtx, ExchangeProtocol response) {
                            doWriteAndFlush(curCtx, response);
                        }
                    });
                } else {
                    //master 默认处理
                    String chatId = value.getBody().getName();
                    ChatSession.ChatStatus status = ChatSession.ChatStatus.valueOf(value.getBody().getKey());
                    ChatSession chatSession = channelManager.updateChatSession(chatId, ctx, false);
                    if (chatSession.isCreateLeader()) {
                        //ack leader
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).text(chatSession.getLeaderWorker(), null, null));
                    } else {
                        //已建立
                        if(chatSession.consistencyWorker()) {
                            //ack follower
                            doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).text(chatSession.getFollowerWorker(), null, null));
                        } else {
                            //ack follower重定向
                            doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()).
                                    text(chatSession.getLeaderWorker(),null, null).
                                    status(ExchangeStatus.wrap(307, chatSession.getLeaderWorker()))
                            );
                            logger.warn("worker not consistency, leader={}, follower={}", chatSession.getLeaderWorker(), chatSession.getFollowerWorker());
                        }
                    }
                }
            case PING:
                if (supportBusiness(value)) {
                    doCustomHandle(new ExchangeResponseConsumer(value, ctx) {
                        @Override
                        public void accept(ChannelHandlerContext curCtx, ExchangeProtocol response) {
                            doWriteAndFlush(curCtx, response);
                        }
                    });
                } else {
                    doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                }
                break;
            case REGISTER:
                if (supportBusiness(value)) {
                    doCustomHandle(new ExchangeResponseConsumer(value, ctx) {
                        @Override
                        public void accept(ChannelHandlerContext curCtx, ExchangeProtocol response) {
                            doWriteAndFlush(curCtx, response);
                        }
                    });
                } else {
                    //worker default
                    RegisterInfo registerInfo = JSONObject.parseObject(value.getBody().getValue(), RegisterInfo.class);
                    if (channelManager.deviceRegister(RegisterInfoHolder.wrap(ctx, registerInfo))) {
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId()));
                    } else {
                        doWriteAndFlush(ctx, ExchangeProtocol.ack(value.getId())
                                .status(ExchangeStatus.wrap(500, "register device failed")));
                    }
                }
                break;
            default:
//                ctx.writeAndFlush(ExchangeProtocol.ack(value.getId()).status(ExchangeStatus.NOT_FOUND));
                logger.error("not fond type.");
                break;
        }
    }
    boolean supportBusiness(ExchangeProtocol value) {
        return (businessHandler != null && businessHandler.support(value.getType()));
    }
    @SuppressWarnings("unchecked")
    void doCustomHandle(ExchangeResponseConsumer context) {
        if(businessHandler != null && context != null) {
            businessHandler.onSubscribe(context);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        metricEventListener.onEvent(MetricEvent.CONNECTION);
        channelManager.register(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        metricEventListener.onEvent(MetricEvent.DISCONNECT);
        channelManager.unregister(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("异常连接：{}, error: {}", ctx.channel().remoteAddress(), cause.getMessage());
        metricEventListener.onEvent(MetricEvent.CONNECTION_ERROR);
        channelManager.unregister(ctx);
        ctx.close();
    }

    /** @param processState true 开始, false 完成 */
    void doProcessing(ChannelHandlerContext ctx, boolean processState){
        channelManager.processing(ctx, processState);
    }
    static Consumer<Boolean> emptyConsumer = aBoolean -> {};
    final ChannelFuture doWriteAndFlush(final ChannelHandlerContext ctx, final ExchangeProtocol value) {
        return doWriteAndFlush(ctx, value, emptyConsumer);
    }
    final ChannelFuture doWriteAndFlush(Supplier<ChannelHandlerContext> contextSupplier, final ExchangeProtocol value, Consumer<Boolean> result) {
        return doWriteAndFlush(contextSupplier.get(), value,  result);
    }
    final ChannelFuture doWriteAndFlush(final ChannelHandlerContext ctx, final ExchangeProtocol value, Consumer<Boolean> result) {
        try {
            doProcessing(ctx, true);
            final long startTimeMillis = ClockUtils.newStartTimeMillis();
            metricEventListener.onEvent(MetricEvent.RESPONSE);
            metricEventListener.onEvent(MetricEvent.RESPONSE_BYTE_SIZE, (17L + value.getBodyLen()));
            return ctx.writeAndFlush(value).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    try {
                        if (future.isSuccess()) {
                            metricEventListener.onEvent(MetricEvent.RESPONSE_TIME_MS, Duration.ofMillis(ClockUtils.onEndMillis(startTimeMillis)));
                            metricEventListener.onEvent(MetricEvent.RESPONSE_SUCCEED);
                        } else {
                            metricEventListener.onEvent(MetricEvent.RESPONSE_FAILED);
                        }
                    } finally {
                        result.accept(future.isSuccess());
                    }
                }
            });
        } finally {
            doProcessing(ctx, false);
        }
    }

    final class ExchangeProtocolHolder {
        final ChannelHandlerContext ctx;
        final ExchangeProtocol value;
        volatile boolean success;
        public ExchangeProtocolHolder(ChannelHandlerContext ctx, ExchangeProtocol value) {
            this.ctx = ctx;
            this.value = value;
        }
        public ExchangeProtocolHolder writeAndFlush() {
            try {
                doProcessing(ctx, true);
                doWriteAndFlush(ctx, value, aBoolean -> {
                    if(aBoolean) {
                        success();
                    }else {
                        failed();
                    }
                });
                success();
            } catch (Exception e) {
                failed();
                throw e;
            } finally {
                doProcessing(ctx, false);
            }
            return this;
        }
        public ExchangeProtocolHolder success() {
            this.success = true;
            return this;
        }
        private ExchangeProtocolHolder failed() {
            this.success = false;
            return this;
        }
        public boolean isSuccess() {
            return success;
        }
    }
    final class SubscribeHolder {
        final Subscribe subscribe;
        final ExchangeProtocol value;
        volatile boolean success;
        AtomicBoolean done = new AtomicBoolean();
        public SubscribeHolder(Subscribe subscribe, ExchangeProtocol value) {
            this.subscribe = subscribe;
            this.value = value;
        }
        public SubscribeHolder writeAndFlush() {
            if(done.compareAndSet(false, true)) {
                try {
                    doProcessing(subscribe.getCtx(), true);
                    final long startTimeMillis = ClockUtils.newStartTimeMillis();
                    metricEventListener.onEvent(MetricEvent.RESPONSE);
                    metricEventListener.onEvent(MetricEvent.RESPONSE_BYTE_SIZE, (17L + value.getBodyLen()));
                    this.subscribe.writeAndFlush(this.value, aBoolean -> {
                        if(aBoolean) {
                            success();
                            metricEventListener.onEvent(MetricEvent.RESPONSE_TIME_MS, Duration.ofMillis(ClockUtils.onEndMillis(startTimeMillis)));
                            metricEventListener.onEvent(MetricEvent.RESPONSE_SUCCEED);
                        } else {
                            failed();
                            done.set(false);
                            metricEventListener.onEvent(MetricEvent.RESPONSE_FAILED);
                        }
                    });
                    success();
                } catch (Exception e) {
                    failed();
                    done.set(false);
                    throw e;
                } finally {
                    doProcessing(subscribe.getCtx(), false);
                }
            }
            return this;
        }
        public boolean isDone() {
            return done.get();
        }
        public SubscribeHolder success() {
            this.success = true;
            return this;
        }
        public SubscribeHolder failed() {
            this.success = false;
            return this;
        }
        public boolean isSuccess() {
            return success;
        }
    }

}