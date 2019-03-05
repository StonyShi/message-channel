package com.stony.mc.concurrent;


import com.stony.mc.ResourceManger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <p>mc-core
 * <p>com.stony.mc.store
 *
 * @author stony
 * @version 下午2:49
 * @since 2019/1/1
 */
public class TaskExecutorFactory {

    final Map<String, ThreadPoolExecutor> cache;

    private TaskExecutorFactory() {
        cache = new HashMap<>(8);

        System.out.println("TaskExecutorFactory register shutdown listener.");
        ResourceManger.register(new ResourceManger.ResourceShutdownListener() {
            @Override
            public void shutdown() {
                if(!cache.isEmpty()) {
                    System.out.println("TaskExecutorFactory Closing pools.");
                    cache.values().stream().forEach(new Consumer<ThreadPoolExecutor>() {
                        @Override
                        public void accept(ThreadPoolExecutor poolExecutor) {
                            TaskExecutorFactory.doClose(poolExecutor);
                        }
                    });
                }
            }
        });
    }
    public ThreadPoolExecutor singleExecutor(String name) {
        return singleExecutor(name, 0);
    }
    public ThreadPoolExecutor singleExecutor(String name, int queueSize) {
        ThreadPoolExecutor threadPoolExecutor = cache.get(name);
        if (threadPoolExecutor == null) {
            synchronized (cache) {
                if ((threadPoolExecutor = cache.get(name)) == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            1,
                            1,
                            0L,
                            TimeUnit.MILLISECONDS,
                            (queueSize == 0) ? new SynchronousQueue<Runnable>() : (queueSize < 0 ? new LinkedBlockingQueue<Runnable>() : new ArrayBlockingQueue<Runnable>(queueSize)),
                            new NamedThreadFactory(String.format("%s-task", name))
                    );
                    cache.put(name, threadPoolExecutor);
                }
            }
        }
        return threadPoolExecutor;
    }
    private ThreadPoolExecutor fixedExecutor(String name, int size, int queueSize) {
        ThreadPoolExecutor threadPoolExecutor = cache.get(name);
        if (threadPoolExecutor == null) {
            synchronized (cache) {
                if ((threadPoolExecutor = cache.get(name)) == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            size,
                            size,
                            0L,
                            TimeUnit.MILLISECONDS,
                            (queueSize == 0) ? new SynchronousQueue<Runnable>() : (queueSize < 0 ? new LinkedBlockingQueue<Runnable>() : new ArrayBlockingQueue<Runnable>(queueSize)),
                            new NamedThreadFactory(String.format("%s-task", name)),
                            new BlockCallerPolicy()
                    );
                    cache.put(name, threadPoolExecutor);
                }
            }
        }
        return threadPoolExecutor;
    }
    /**
     * default use @{link TaskTransferQueue}
     * @param name
     * @param size
     * @param maxSize
     * @param keepLiveMS
     * @return
     */
    public ThreadPoolExecutor elasticExecutor(String name, int size, int maxSize, long keepLiveMS) {
        ThreadPoolExecutor threadPoolExecutor = cache.get(name);
        if (threadPoolExecutor == null) {
            synchronized (cache) {
                if ((threadPoolExecutor = cache.get(name)) == null) {
                    TaskTransferQueue queue = new TaskTransferQueue();
                    threadPoolExecutor = new ThreadPoolExecutor(size,
                            maxSize,
                            keepLiveMS,
                            TimeUnit.MILLISECONDS,
                            queue,
                            new NamedThreadFactory(String.format("%s-task", name)),
                            new BlockCallerPolicy());
                    queue.setExecutor(threadPoolExecutor);
                    cache.put(name, threadPoolExecutor);
                }
            }
        }
        return threadPoolExecutor;
    }

    public static ThreadPoolExecutor getElasticExecutor(String name, int size, int maxSize, long keepLive, TimeUnit unit) {
        return TaskExecutorFactory.getInstance().elasticExecutor(name, size, maxSize, unit.convert(keepLive, TimeUnit.MILLISECONDS));
    }
    public static ThreadPoolExecutor getElasticExecutor(String name, int size, int maxSize, long keepLiveMS) {
        return TaskExecutorFactory.getInstance().elasticExecutor(name, size, maxSize, keepLiveMS);
    }
    public static ThreadPoolExecutor getElasticExecutor(String name, int size) {
        return TaskExecutorFactory.getInstance().elasticExecutor(name, size, size, 0);
    }
    public static ThreadPoolExecutor getSingleExecutor(String name) {
        return TaskExecutorFactory.getInstance().singleExecutor(name);
    }
    public static ThreadPoolExecutor getFixedExecutor(String name, int size, int queueSize) {
        return TaskExecutorFactory.getInstance().fixedExecutor(name, size, queueSize);
    }



    public static void doClose(ThreadPoolExecutor threadPoolExecutor){
        try {
            if(check(threadPoolExecutor)) {
                System.out.println(">>> Closing thread pool executor. ");
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPoolExecutor.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(">>> Closing thread pool executor error: " + e.getMessage());
            threadPoolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public static boolean check(ThreadPoolExecutor threadPoolExecutor) {
        return (threadPoolExecutor != null
                && (!threadPoolExecutor.isShutdown() || !threadPoolExecutor.isTerminated())
        );
    }
    /**
     *
     * @param threads
     * @param queues   if queues equals 0 return SynchronousQueue,if > 0 ArrayBlockingQueue if < 0 LinkedBlockingQueue
     * @return
     */
    public static ThreadPoolExecutor newExecutor(int threads, int maxThreads, int queues){
        return new ThreadPoolExecutor(threads, maxThreads, 0, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>()
                        : (queues < 0 ? new LinkedBlockingQueue<Runnable>() : new ArrayBlockingQueue<Runnable>(queues)),
                new NamedThreadFactory(true), new ThreadPoolExecutor.AbortPolicy());
    }

    public static TaskExecutorFactory getInstance() {
        return TaskExecutorFactoryHolder.Instance;
    }



    private static class TaskExecutorFactoryHolder {
        private static TaskExecutorFactory Instance = new TaskExecutorFactory();
    }
}