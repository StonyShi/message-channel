package com.stony.mc.future;

import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.listener.SubscribeListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午4:42
 * @since 2019/1/4
 */
public class SimpleResultFutureStore implements ResultFutureStore<ExchangeProtocol> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleResultFutureStore.class);
    private volatile boolean closed = false;
    private SubscribeListener subscribeListener;

    final Map<Long, ResultFuture> futureMap = new ConcurrentHashMap<>(512);

    public void put(ResultFuture future) {
        futureMap.put(future.getId(), future);
    }

    public void update(long key, ExchangeProtocol value) {
        ResultFuture f = futureMap.remove(key);
        doUpdate(f, value, null);
    }

    public void update(long key, ExchangeProtocol value, ChannelHandlerContext ctx) {
        ResultFuture f = futureMap.remove(key);
        doUpdate(f, value, ctx);
    }

    void doUpdate(ResultFuture f, ExchangeProtocol value, ChannelHandlerContext ctx) {
        try {
            if (f != null) {
                f.set(value);
                if (ctx != null) f.setContext(ctx);
            }
        } finally {
            onDoSubscribe(value);
        }
    }

    public ResultFuture get(long key) {
        return futureMap.get(key);
    }

    public ResultFuture del(long key) {
        return futureMap.remove(key);
    }

    @Override
    public void shutdown() {
        if (!closed) {
            synchronized (this) {
                try {
                    close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void remove(ChannelHandlerContext ctx) {
        long[] ids = futureMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getContext()!= null && entry.getValue().getContext().equals(ctx))
                .mapToLong(Map.Entry::getKey).toArray();
        ResultFuture f;
        for (long id : ids) {
            f = del(id);
            if(f != null) {
                f.set(null);
            }
        }
    }

    @Override
    public void close() throws Exception {
        logger.info("close futureMap: " + futureMap);
        if (!futureMap.isEmpty()) {
            try {
                futureMap.values().forEach(v -> {
                    if (!v.isDone()) {
                        v.cancel(true);
                    }
                });
            } finally {
                closed = true;
                futureMap.clear();
            }
        } else {
            closed = true;
        }
    }

    public void setSubscribeListener(SubscribeListener subscribeListener) {
        this.subscribeListener = subscribeListener;
    }
    @SuppressWarnings("unchecked")
    public SubscribeListener getSubscribeListener() {
        return subscribeListener;
    }

    @SuppressWarnings("unchecked")
    public void onDoSubscribe(ExchangeProtocol value) {
        if(subscribeListener != null && value != null) {
            if (subscribeListener.support(value.getType())) {
                subscribeListener.onSubscribe(value);
            }
        }
    }
}