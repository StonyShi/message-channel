package com.stony.mc.session;

import com.stony.mc.future.ResultFuture;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.ChatListener;
import com.stony.mc.listener.SubscribeListener;
import com.stony.mc.listener.TopicRecord;
import com.stony.mc.listener.TopicRecordListener;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeTypeEnum;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.lang.management.ManagementFactory.getMemoryMXBean;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午12:00
 * @since 2019/1/17
 */
public class WorkerServerTest {

    private final RuntimeMXBean rtMxBean = getRuntimeMXBean();
    private final OperatingSystemMXBean osMxBean = getOperatingSystemMXBean();
    private final MemoryMXBean memMxBean = getMemoryMXBean();
    @Test
    public void test_master() throws Exception {
        MasterServer server = new MasterServer(4088);
        System.out.println(">>>>>>>>>>>PID:" + rtMxBean.getName());
        System.out.println(">>>>>>>>>>>Load:" + osMxBean.getSystemLoadAverage());
        System.out.println(">>>>>>>>>>>heap use:" + memMxBean.getHeapMemoryUsage());
        System.out.println(">>>>>>>>>>>non heap use:" + memMxBean.getNonHeapMemoryUsage());
        server.startAndWait();
    }

    @Test
    public void test_worker_01() throws Exception {
        WorkerServer workerServer = new WorkerServer(4091, new String[]{"localhost:4088"});
        workerServer.setBalance(false);
        workerServer.startAndWait();
    }
    @Test
    public void test_worker_02() throws Exception {
        WorkerServer workerServer = new WorkerServer(4092, new String[]{"localhost:4088"});
        workerServer.setBalance(false);
        workerServer.startAndWait();
    }
    @Test
    public void test_worker_03() throws Exception {
        WorkerServer workerServer = new WorkerServer(4093, new String[]{"localhost:4088"});
        workerServer.setBalance(false);
        workerServer.startAndWait();
    }
    @Test
    public void test_worker_04() throws Exception {
        WorkerServer workerServer = new WorkerServer(4094, new String[]{"localhost:4088"});
        workerServer.setBalance(false);
        workerServer.startAndWait();
    }
    @Test
    public void test_worker_05() throws Exception {
        WorkerServer workerServer = new WorkerServer(4095, new String[]{"localhost:4088"});
        workerServer.setBalance(false);
        workerServer.startAndWait();
    }

    @Test
    public void test_consumer() throws Exception {
        SimpleConsumer consumer = new SimpleConsumer(3000, new String[]{"localhost:4088"});
        consumer.connect();

        consumer.subscribe("my", "ddx", null, new TopicRecordListener() {
            @Override
            public void onSubscribe(TopicRecord v) {
                System.out.println("订阅：" + v);
            }
        });

        consumer.waitTillShutdown();
    }
    @Test
    public void test_consumer_02() throws Exception {
        SimpleConsumer consumer = new SimpleConsumer(3000, new String[]{"localhost:4088"});
        consumer.connect();

        consumer.subscribe("my", "ddx", null, new TopicRecordListener() {
            @Override
            public void onSubscribe(TopicRecord v) {
                System.out.println("订阅：" + v);
            }
        });

        consumer.waitTillShutdown();
    }
    @Test
    public void test_producer_01() throws Exception {
        SimpleProducer producer = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer.connect();
        RegisterInfo registerInfo = new RegisterInfo("38902234");
        registerInfo.addTag("driver");
        registerInfo.setDevice("android_0122_343");
        System.out.println(producer.register(registerInfo).get());
        System.out.println("------注册完成-------");
        for (int i = 0; i < 300; i++) {
            System.out.println(producer.sendText("ddx", "key_" + i, "value_" + i).get());
        }
        System.out.println("---------发送完成----");
        Thread.sleep(9000L);
        for (int i = 0; i < 300; i++) {
            System.out.println(producer.sendText("ddx", "key_" + i, "value_" + i).get());
        }
        System.out.println("---------发送完成----");
        Thread.sleep(9000L);
        producer.shutdown();
    }

    @Test
    public void test_producer_02() throws Exception {
        SimpleProducer producer = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer.connect();
        RegisterInfo registerInfo = new RegisterInfo("38902210");
        registerInfo.addTag("driver");
        registerInfo.setDevice("android_010_223");
        System.out.println(producer.register(registerInfo).get());
        System.out.println("------注册完成-------");
        for (int i = 0; i < 300; i++) {
            System.out.println(producer.sendJson("ddx", "key_" + i, ("{\"value\":"+i+"}")).get());
        }
        System.out.println("---------发送完成----");
        Thread.sleep(9000L);
        for (int i = 0; i < 300; i++) {
            System.out.println(producer.sendText("ddx", "key_" + i, "value_" + i).get());
        }
        System.out.println("---------发送完成----");
        Thread.sleep(9000L);
        producer.shutdown();
    }

    String chatId = "131415678";
    @Test
    public void test_chat_01() throws Exception {
        SimpleProducer producer = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer.connect();
        producer.createChat(chatId);
        TimeUnit.SECONDS.sleep(50);
        producer.sendChat(chatId, "我好呀！");
        producer.shutdown();
    }
    @Test
    public void test_chat_02() throws Exception {
        SimpleProducer producer = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer.connect();
        TimeUnit.SECONDS.sleep(15);
        producer.createChat(chatId);
        producer.sendChat(chatId, "你好呀！");
        producer.shutdown();
    }
    @Test
    public void test_chat() throws Exception {
        SimpleProducer producer = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer.setSubscribeListener((ChatListener) v -> System.out.println("Chat : " + v));
        producer.connect();
        producer.createChat(chatId);

        SimpleProducer producer2 = new SimpleProducer(3000, new String[]{"localhost:4088"});
        producer2.setSubscribeListener((ChatListener) v -> System.out.println("Chat : " + v));
        producer2.connect();
        producer2.createChat(chatId);

        ResultFuture f1 = producer.sendChat(chatId, "我说一！");
        ResultFuture f2 = producer2.sendChat(chatId, "你说22！");

        System.out.println("---------------f11111  ");
        System.out.println(f1.get());
        System.out.println("---------------f2222  ");
        System.out.println(f2.get());
        producer.shutdown();
        producer2.shutdown();

    }
    @Test
    public void test_balance() throws InterruptedException {
        List<BaseClient> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BaseClient client = new BaseClient(){}.idleWriteTime(0).idleReadTime(0);
            try {
                client.connect("localhost", 4091);
            } catch (Exception e) {}
            list.add(client);
        }
        for (int i = 0; i < 3; i++) {
            BaseClient client = new BaseClient(){}.idleWriteTime(0).idleReadTime(0);
            try {
                client.connect("localhost", 4092);
            } catch (Exception e) {}
            list.add(client);
        }
        for (int i = 0; i < 10; i++) {
            BaseClient client = new BaseClient(){}.idleWriteTime(0).idleReadTime(0);
            try {
                client.connect("localhost", 4091);
            } catch (Exception e) {}
            list.add(client);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                for (BaseClient client : list) {
                    client.shutdown();
                }
            }
        });
        System.out.println("等待关闭客户端。。。。");
        Thread.sleep(3000000L);
        System.out.println("关闭客户端......................");

        for (BaseClient client : list) {
            client.shutdown();
        }

    }
    @Test
    public void test_allocation(){
        BaseClient heartbeat = new BaseClient(){};
        try {
            heartbeat.connect("localhost", 4088, 3);

            IdGenerator idWorker = SimpleIdGenerator.getInstance();

            ResultFuture future = heartbeat.submit(
                    ExchangeProtocol
                            .create(idWorker.nextId())
                            .type(ExchangeTypeEnum.ALLOCATION)
                            .text("true", null, null)
            );
            System.out.println(future.get().getBody());
            System.out.println("--------------------");
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
}