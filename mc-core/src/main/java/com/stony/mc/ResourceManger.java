package com.stony.mc;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>geo-pickup-data-mining
 * <p>com.yongche.pickup
 *
 * @author stony
 * @version 下午5:31
 * @since 2018/2/27
 */
public class ResourceManger {
    static final LinkedList<ResourceListener> listenerList = new LinkedList<ResourceListener>();
    static final AtomicBoolean startup = new AtomicBoolean(false);
    static final AtomicBoolean shutdown = new AtomicBoolean(false);
    static {
        System.out.println("ResourceManger register shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new Thread("ResourceManger-Thread"){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public static void register(ResourceListener listener) {
        listenerList.add(listener);
    }
    public static void register(Consumer<Void> consumer) {
        register(new ResourceShutdownListener() {
            @Override
            public void shutdown() {
                consumer.accept(null);
            }
        });
    }

    public synchronized static void shutdown() {
        if (!shutdown.get()) {
            if (shutdown.compareAndSet(false, true)) {
                LinkedList<ResourceListener> listeners = listenerList;
                for (ResourceListener listener : listeners) {
                    doShutdown(listener);
                }
                listenerList.clear();
            }
        }
    }
    static void doShutdown(ResourceListener listener) {
        try {
            listener.shutdown();
        } catch (Throwable e) {
            System.out.println(String.format("listener: %s, shutdown error: %s", listener, e.getMessage()));
        }
    }

    public synchronized static void startup() {
        if (!startup.get()) {
            if (startup.compareAndSet(false, true)) {
                LinkedList<ResourceListener> listeners = (LinkedList<ResourceListener>) listenerList.clone();
                for (ResourceListener listener : listeners) {
                    doStartup(listener);
                }
            }
        }
    }
    static void doStartup(ResourceListener listener) {
        try {
            listener.startup();
        } catch (Throwable e) {
            System.out.println(String.format("listener: %s, startup error: %s", listener, e.getMessage()));
            e.printStackTrace();
        }
    }

    public static interface ResourceListener {
        void startup();
        void shutdown();
    }
    public static abstract class ResourceShutdownListener implements ResourceListener {
        @Override
        public void startup() {}
    }
}
