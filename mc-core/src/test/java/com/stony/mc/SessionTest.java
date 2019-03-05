package com.stony.mc;

import com.stony.mc.concurrent.TaskExecutorFactory;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureListenable;
import com.stony.mc.future.ResultFutureStore;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.ResultFutureListener;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.listener.SubscriberProcessor;
import com.stony.mc.metrics.MetricStatistics;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;
import com.stony.mc.protocol.ExchangeTypeEnum;
import com.stony.mc.session.BaseClient;
import com.stony.mc.session.BaseServer;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>mc-core
 * <p>PACKAGE_NAME
 *
 * @author stony
 * @version 下午6:47
 * @since 2019/1/10
 */
public class SessionTest {
    static int port = 4091;
    class BaseServerT extends BaseServer<BaseServerT> {
        public BaseServerT(String serverName, int port) {
            super(serverName, port);
        }
        public BaseServerT(int port) {
            super(port);
        }
    }
    class BaseClientT extends BaseClient {
        public BaseClientT() {
        }

        public BaseClientT(int timeoutMs) {
            super(timeoutMs);
        }

        public BaseClientT(ResultFutureStore futureStore) {
            super(futureStore);
        }

        public BaseClientT(ResultFutureStore futureStore, int timeoutMs) {
            super(futureStore, timeoutMs);
        }

        public BaseClientT(String serverHost, int serverPort, ResultFutureStore futureStore, int timeoutMs) {
            super(serverHost, serverPort, futureStore, timeoutMs);
        }
    }

    @Test
    public void test_master() throws Exception {
        BaseServerT server = new BaseServerT(4088);
        SubscriberProcessor processor = new SubscriberProcessor(20, 100, 60000){
            @Override
            public boolean support(ExchangeTypeEnum typeEnum) {
                return ExchangeTypeEnum.PING == typeEnum;
            }
        };
        processor.addListener(v -> {
            System.out.println("Master 接收: " + v);
        });
        server.subscribeListener(processor).startAndWait();
    }
    @Test
    public void test_client_retry(){
        BaseClient heartbeat = new BaseClientT();
        try {
            heartbeat.connect("localhost", 4088, 3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            heartbeat.shutdown();
        }
    }

    @Test
    public void test_worker() throws Exception {
//        test_server(port);
        test_server(4090);
    }

    public void test_server(int port) throws Exception {
        final BaseServer server = new BaseServerT(port);
        final MetricStatistics metricStatistics = new MetricStatistics();
        SubscriberProcessor processor = new SubscriberProcessor(20, 100, 60000){
            @Override
            public boolean support(ExchangeTypeEnum typeEnum) {
                return false;
            }
        };
        processor.addListener(new SubscribeListener() {
            @Override
            public void onSubscribe(ExchangeProtocol v) {
                System.out.println("Worker 接收: " + v);
            }
            @Override
            public boolean support(ExchangeTypeEnum typeEnum) {
                return ExchangeTypeEnum.MESSAGE == typeEnum;
            }
        });
        server.subscribeListener(processor);
        server.metricStatistics(metricStatistics);
        int heartbeatMS = 3000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                BaseClient heartbeat = new BaseClientT();
                beat_loop:
                while (true) {
                    System.out.println(String.format("Master[%s:%d] is beging.", "localhost", 4088));
                    for (int i = 0; i < 3; i++) {
                        try {
                            heartbeat.connect("localhost", 4088);
                            break;
                        } catch (Exception e) {
                            try {
                                System.out.println("连接失败等待重试: " + (i+1) + "次");
                                TimeUnit.MILLISECONDS.sleep(heartbeatMS*(2*(i+1)));
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if(!heartbeat.isLive()) {
                        server.shutdown();
                        throw new RuntimeException(String.format("Master[%s:%d] is not connected.", "localhost", 4088));
                    }
                    System.out.println(String.format("Master[%s:%d] is connected.", "localhost", 4088));
                    while (heartbeat.isLive()) {
                        inner_loop: for (int i = 0; i < 3; i++) {
                            ResultFuture future = heartbeat.submit(
                                    ExchangeProtocol
                                            .ping(System.currentTimeMillis())
                                            .json(metricStatistics.getStatisticsJson(server.getServerPort()), "heartbeat", null)
                            );
                            try {
                                ExchangeProtocol value = future.get(heartbeatMS, TimeUnit.MILLISECONDS);
                                if(value.getStatus() != ExchangeStatus.OK) {
                                    System.out.println("heartbeat ack: " + value.getStatus());
                                }
                            } catch (TimeoutException te) {
                                System.out.println(String.format("Master[%s:%d] is heartbeat Timeout.", "localhost", 4088));
                                if(!heartbeat.isLive()) {
                                    continue beat_loop;
                                }
                                continue inner_loop;
                            } catch (Exception e) {
                                System.out.println("heartbeat error: " + e);
                                continue beat_loop;
                            }
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(heartbeatMS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "worker-heartbeat").start();

        server.startAndWait();
    }

    @Test
    public void test_allocation(){
        BaseClient heartbeat = new BaseClientT();
        try {
            heartbeat.connect("localhost", 4088, 3);

            IdGenerator idWorker = SimpleIdGenerator.getInstance();
            for (int i = 0; i < 5; i++) {
                System.out.println(
                        heartbeat.submit(
                                ExchangeProtocol.create(idWorker.nextId())
                                        .type(ExchangeTypeEnum.ALLOCATION)
                        ).get().getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            heartbeat.shutdown();
        }
    }
    @Test
    public void test_consumer() throws Exception {
        test_consumer("my2", "ddx");
    }
    public void test_consumer(String group, String topic) throws Exception {
        final CountDownLatch startupLatch = new CountDownLatch(1);
        IdGenerator idWorker = SimpleIdGenerator.getInstance();
        BaseClient client = new BaseClientT();
        AtomicLong consumerCount = new AtomicLong();
        try {
            client.connect("localhost", port);

            ResultFutureListenable fl = client.invoke(ExchangeProtocol.ping(idWorker.nextId()));
            fl.listener(new ResultFutureListener<ExchangeProtocol>() {
                @Override
                public void onSuccess(ExchangeProtocol v) {
                    System.out.println("ping: " + v);
                }
                @Override
                public void onFailed(Throwable e) {
                    System.out.println("ping error: "+e.getMessage());
                }
            });
//            ResultFuture f = client.ping(System.currentTimeMillis());
//            try {
//                System.out.println("ping: " + f.get());
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }

            SubscribeListener<ExchangeProtocol> subscribeListener = new SubscribeListener<ExchangeProtocol>() {
                @Override
                public void onSubscribe(ExchangeProtocol protocol) {
                    consumerCount.incrementAndGet();
                    System.out.println("Consumer: " + protocol);
                }
            };
            client.setSubscribeListener(subscribeListener);

            ResultFuture f2 = client.submit(ExchangeProtocol.subscribe(idWorker.nextId(), group, topic, null));
            try {
                System.out.println("subscribe: " + f2.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println("Consumer订阅完成-----");

            Runtime.getRuntime().addShutdownHook(new Thread("Consumer-Hook") {
                @Override
                public void run() {
                    startupLatch.countDown();
                    System.out.println("Consumer消费完成：" + consumerCount.get());
                }
            });

            client.waitTillShutdown();
        } finally {
            client.shutdown();
        }
    }
    @Test
    public void test_producer_branch() throws Exception {
        int threads = 10;
        ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test_producer_branch", threads, threads*2, 10000);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    test_producer(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        System.out.println("开始生产-------");
        latch.await();
        System.out.println("开始生产完成-------");
        executor.shutdown();
        System.out.println("关闭生产线程池-----");
    }
    @Test
    public void test_producer() throws Exception {
        test_producer(100);
    }
    public void test_producer(int msgSize) throws Exception {
        IdGenerator idWorker = SimpleIdGenerator.getInstance();
        BaseClient client = new BaseClientT();
        CountDownLatch downLatch = new CountDownLatch(msgSize);
        System.out.println("Producer 发送消费：" + msgSize);
        try {
            client.connect("localhost", port);
            for (int i = 0; i < msgSize; i++) {

                ExchangeProtocol value = ExchangeProtocol.create(idWorker.nextId());
                String v = "你好_" + i;
                value.text(v, "ddx", ""+i);

                ResultFutureListenable fl = client.invoke(value);
                fl.listener(new ResultFutureListener<ExchangeProtocol>() {
                    @Override
                    public void onSuccess(ExchangeProtocol v) {
                        downLatch.countDown();
                        System.out.println("Ack: " + v);

                    }
                    @Override
                    public void onFailed(Throwable e) {
                        downLatch.countDown();
                        System.out.println("ack error:  "+e.getMessage());
                    }
                });
            }
            System.out.println("发送完成,等待响应.....");
            downLatch.await();
//            client.waitTillShutdown();
            System.out.println("发送结束了...");
        }finally {
            client.shutdown();
        }
    }

}
