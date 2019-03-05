package com.stony.mc.listener;

import com.stony.mc.concurrent.TaskExecutorFactory;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>mc-core
 * <p>com.stony.mc.future
 *
 * @author stony
 * @version 下午3:32
 * @since 2019/1/10
 */
public class SubscriberProcessor implements SubscribeListener {

    private volatile SubscribeListener[] listeners;
    private final ThreadPoolExecutor executor;

    private String name = getClass().getSimpleName();
    public SubscriberProcessor() {
        this(1);
    }
    public SubscriberProcessor(int threads) {
        this(threads, threads);
    }
    public SubscriberProcessor(int threads, int maxThreads) {
        this(threads, maxThreads, 0);
    }
    public SubscriberProcessor(int threads, int maxThreads, long keepLiveTimeMS) {
        this.executor = TaskExecutorFactory.getElasticExecutor(name, threads, maxThreads, keepLiveTimeMS);
    }

    public SubscriberProcessor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void onSubscribe(ExchangeProtocol value) {
        if(listeners != null && value != null) {
            if(this.support(value.getType())) {
                executor.execute(() -> onDoSubscribe(value));
            }
        }
    }
    public synchronized void addListener(SubscribeListener listener) {
        if(listeners == null) {
            listeners = new SubscribeListener[1];
            listeners[0] = listener;
        } else {
            int len = listeners.length;
            SubscribeListener[] temp = new SubscribeListener[len + 1];
            System.arraycopy(listeners, 0, temp, 0, len);
            temp[len] = listener;
            this.listeners = temp;
        }
    }
    @SuppressWarnings("unchecked")
    final synchronized void onDoSubscribe(ExchangeProtocol value) {
        if(listeners != null) {
            SubscribeListener[] _listeners = listeners;
            for(SubscribeListener listener: _listeners) {
                try {
                    listener.onSubscribe(value);
                } catch (Throwable e){
                    //pass
                }
            }
        }
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean support(ExchangeTypeEnum typeEnum) {
        return true;
    }
}
