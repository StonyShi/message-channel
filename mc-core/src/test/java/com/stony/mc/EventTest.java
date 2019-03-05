package com.stony.mc;

import com.alibaba.fastjson.JSON;
import com.stony.mc.concurrent.TaskExecutorFactory;
import com.stony.mc.handler.ClientHandler;
import com.stony.mc.handler.ConvertEncoder;
import com.stony.mc.handler.ExchangeCodecAdapter;
import com.stony.mc.handler.ServerHandler;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.*;
import com.stony.mc.manager.ChannelManager;
import com.stony.mc.manager.Subscribe;
import com.stony.mc.manager.SubscribeInfo;
import com.stony.mc.protocol.BaseExchangeBody;
import com.stony.mc.protocol.ExchangeProtocol;
import com.stony.mc.protocol.ExchangeStatus;
import com.stony.mc.future.ResultFuture;
import com.stony.mc.future.ResultFutureStore;
import com.stony.mc.future.SimpleResultFutureStore;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>mc-core
 * <p>PACKAGE_NAME
 *
 * @author stony
 * @version 上午10:10
 * @since 2019/1/3
 */
public class EventTest {

    @Test
    public void test_server() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()+1);

        try {
            final ChannelManager channelManager = ChannelManager.create();
            ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test_server", 20, 20, 0);

            ServerBootstrap server = new ServerBootstrap();

            server.group(boss, worker).channel(NioServerSocketChannel.class);

//            server.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
//                    .option(ChannelOption.TCP_NODELAY, true);
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ExchangeCodecAdapter adapter = new ExchangeCodecAdapter();
                    socketChannel.pipeline()
//                            .addFirst("BytesInspector", new BytesInspector(new EventsSubject()))
                            .addLast("ExchangeEncoder", adapter.getEncoder())  //out
                            .addLast("ExchangeDecoder", adapter.getDecoder())  //in
                            .addLast("ServerHandler", new ServerHandler(channelManager, executor))     //in

                    ;
                }
            });
            server.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = server.bind(4088).sync();
            System.out.println("start server : " + future.channel());
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    static String topic = "ddx";
    @Test
    public void test_consumer() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        ResultFutureStore<ExchangeProtocol> futureStore = new SimpleResultFutureStore();
        final ConvertEncoder convert = new ConvertEncoder(futureStore);
        final ClientHandler clientHandler = new ClientHandler(futureStore, null);
        final CountDownLatch startupLatch = new CountDownLatch(1);
        try {
            Bootstrap client = new Bootstrap();
            client.group(group)
                    .remoteAddress("localhost", 4088)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
            client.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ExchangeCodecAdapter adapter = new ExchangeCodecAdapter();
                    channel.pipeline()
                            .addLast("ExchangeEncoder", adapter.getEncoder()) //out
                            .addLast("ConvertEncoder", convert)               //out
                            .addLast("ExchangeDecoder", adapter.getDecoder()) //in
                            .addLast("ClientHandler", clientHandler)          //in
                    ;
                }
            });

            ChannelFuture future = client.connect().sync();
            Channel originChannel = future.channel();

            ExchangeProtocol ping = ExchangeProtocol.ping(System.currentTimeMillis());
            ResultFuture resultFuture = ResultFuture.wrap(ping);

            ChannelFuture p = originChannel.writeAndFlush(resultFuture);
            try {
                System.out.println("ping: " + resultFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            SubscribeListener<ExchangeProtocol> subscribeListener = new SubscribeListener<ExchangeProtocol>() {
                @Override
                public void onSubscribe(ExchangeProtocol protocol) {
                    System.out.println("接受订阅: " + protocol);
                }
            };
            futureStore.setSubscribeListener(subscribeListener);

            ExchangeProtocol sub = ExchangeProtocol.subscribe(System.currentTimeMillis(), topic, null);
//            ExchangeProtocol sub = ExchangeProtocol.subscribe(System.currentTimeMillis(), "java", topic, null);
            originChannel.writeAndFlush(sub).sync();


            System.out.println("---------订阅完成----");
            Runtime.getRuntime().addShutdownHook(new Thread("sub-c"){
                @Override
                public void run() {
                    System.out.println("--------关闭消费者------");
                    startupLatch.countDown();
                }
            });
            startupLatch.await();
        } finally {
            group.shutdownGracefully(0, 10, TimeUnit.SECONDS);
            futureStore.shutdown();
        }
    }

    @Test
    public void test_producer_branch() throws InterruptedException {
        int threads = 10;
        ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test_producer_branch", threads, threads*2, 10000);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        test_producer(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
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
    public void test_producer() throws InterruptedException {
        test_producer(10);
    }
    public void test_producer(int size) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        ResultFutureStore futureStore = new SimpleResultFutureStore();
        final ConvertEncoder convert = new ConvertEncoder(futureStore);
        final ClientHandler clientHandler = new ClientHandler(futureStore, null);
        try {
            Bootstrap client = new Bootstrap();
            client.group(group)
                    .remoteAddress("localhost", 4088)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
            client.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ExchangeCodecAdapter adapter = new ExchangeCodecAdapter();
                    channel.pipeline()
                            .addLast("ExchangeEncoder", adapter.getEncoder())
                            .addLast("ConvertEncoder", convert)
                            .addLast("ExchangeDecoder", adapter.getDecoder())
                            .addLast("ClientHandler", clientHandler)
                    ;
                }
            });

            ChannelFuture future = client.connect().sync();
            Channel originChannel = future.channel();

            ExchangeProtocol ping = ExchangeProtocol.ping(System.currentTimeMillis());
            ResultFuture resultFuture = ResultFuture.wrap(ping);

            ChannelFuture p = originChannel.writeAndFlush(resultFuture);
            try {
                System.out.println("ping: " + resultFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            IdGenerator idWorker = SimpleIdGenerator.getInstance();
            final Lock lock = new ReentrantLock();
            final Condition condition = lock.newCondition();
            CountDownLatch downLatch = new CountDownLatch(size);
            for (int i = 0; i < size; i++) {
                try {
                    lock.lock();
                    ExchangeProtocol value = ExchangeProtocol.create(idWorker.nextId());
                    String v = "你好_" + i;

                    value.text(v, topic, ""+i);

                    ChannelFuture f = originChannel.writeAndFlush(value).sync();
                    f.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            downLatch.countDown();
                        }
                    });
                } finally {
                    lock.unlock();
                }
            }

            resultFuture = ResultFuture.wrap(ExchangeProtocol.create(idWorker.nextId()));
            originChannel.writeAndFlush(resultFuture);
            try {
                System.out.println("msg: " + resultFuture.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            downLatch.await();
            System.out.println("---------生产完成----");

        } finally {
            group.shutdownGracefully(0, 10, TimeUnit.SECONDS);
            futureStore.shutdown();
        }
    }

    @Test
    public void test_status(){
        System.out.println((short) ExchangeStatus.BAD_GATEWAY.getCode());

        System.out.println((byte) 100);
        System.out.println((byte) 128);

        String v = "你好_" + 10;

        System.out.println(v.getBytes(StandardCharsets.UTF_8).length+4);
    }

    @Test
    public void test_comprocess() throws CompressorException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream gizpOut = new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.GZIP, out);

        gizpOut.write("你好的水电费水电费撒地方撒点发送到发送到发送地方".getBytes(StandardCharsets.UTF_8));

        gizpOut.flush();
        gizpOut.close();

        System.out.println(Arrays.toString(out.toByteArray()));
//        System.out.println(out.toByteArray().length);
        System.out.println("你好的水电费水电费撒地方撒点发送到发送到发送地方".getBytes(StandardCharsets.UTF_8).length);




        ByteArrayInputStream in =
                new ByteArrayInputStream(out.toByteArray());

        InputStream gzi = new GzipCompressorInputStream(in);

//        CompressorInputStream gizpIn = new CompressorStreamFactory()
//                .createCompressorInputStream(CompressorStreamFactory.GZIP, in);

        System.out.println(IOUtils.toByteArray(gzi).length);

//        gizpOut.close();
        gzi.close();
        in.close();
    }
    @Test
    public void test_gzip2() throws CompressorException, IOException {
        String str = "你你好你好呀好的水电费水电费撒地方撒点发送到发送到发送地方";
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        System.out.println("src : " + data.length);
        byte[] com = Utils.gzip2(data);
        System.out.println("gzip : " + com.length);

        byte[] ss = Utils.ungzip2(com);
        System.out.println("src : " + ss.length);
    }
    @Test
    public void test_gzip() throws IOException {
        String str = "你你好你好呀好的水电费水电费撒地方撒点发送到发送到发送地方";
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        System.out.println("src : " + data.length);
        byte[] com = Utils.gzip(data);
        System.out.println("gzip : " + com.length);

        byte[] ss = Utils.ungzip(com);
        System.out.println("src : " + ss.length);
    }

    @Test
    public void test_snappy() throws IOException {
        String str = "你你好你好呀好的水电费水电费撒地方撒点发送到发送到发送地方";
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        System.out.println("src : " + data.length);
        byte[] com = Utils.snappy(data);
        System.out.println("snappy : " + com.length);

        byte[] ss = Utils.unsnappy(com);
        System.out.println("src : " + ss.length);
    }

    @Test
    public void test_lz4() throws IOException {
        String str = "你你好你好呀好的水电费水电费撒地方撒点发送到发送到发送地方";
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        System.out.println("src : " + data.length);
        byte[] com = Utils.lz4(data);
        System.out.println("lz4 : " + com.length);

        byte[] ss = Utils.unlz4(com);
        System.out.println("src : " + ss.length);
    }


    @Test
    public void test_257(){
        System.out.println(Utils.indexOf(BaseExchangeBody.IMAGE_EXTENSIONS, "ico"));
        System.out.println(Utils.indexOf(BaseExchangeBody.IMAGE_EXTENSIONS, "png"));
        System.out.println(Utils.indexOf(BaseExchangeBody.IMAGE_EXTENSIONS, "pcx"));
        System.out.println(Arrays.binarySearch(BaseExchangeBody.IMAGE_EXTENSIONS, "ico", (o1, o2) -> o1.equals(o2)?0:1));
        System.out.println(Arrays.binarySearch(BaseExchangeBody.IMAGE_EXTENSIONS, "png", (o1, o2) -> o1.equals(o2)?0:1));
        System.out.println(Arrays.binarySearch(BaseExchangeBody.IMAGE_EXTENSIONS, "pcx", (o1, o2) -> o1.equals(o2)?0:1));
    }

    @Test
    public void test_268(){
        SubscribeInfo info = new SubscribeInfo("ddx", null, null);
        System.out.println(JSON.toJSONString(info));

        System.out.println(JSON.parseObject(JSON.toJSONString(info)));
    }

    @Test
    public void test_348(){
        //写入最少
        ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
        List<Subscribe> list = new ArrayList<>(128);
        list.add(new Subscribe(context, "xx", "1", null).writeCount(10).writeCountSuccess(9).writeCountFailed(1));
        list.add(new Subscribe(context, "xx", "2", null).writeCount(10).writeCountSuccess(8).writeCountFailed(2));


        System.out.println(list.stream().sorted(Subscribe.getComparator()).findFirst().get().getTopic());

        Assert.assertEquals(list.stream().sorted(Subscribe.getComparator()).findFirst().get().getTopic(), "2");

        list.add(new Subscribe(context, "xx", "3", null).writeCount(9).writeCountSuccess(8).writeCountFailed(0));
        Assert.assertEquals(list.stream().sorted(Subscribe.getComparator()).findFirst().get().getTopic(), "3");

    }
    @Test
    public void test_371(){
        System.out.println(UUID.randomUUID());
        System.out.println(UUID.randomUUID());
    }
    @Test
    public void test_377(){
        SubscriberProcessor subscribeManager = new SubscriberProcessor();

        subscribeManager.addListener(new SubscribeListener() {
            @Override
            public void onSubscribe(ExchangeProtocol exchangeProtocol) {
                System.out.println("A>>> " + exchangeProtocol);
            }
        });

        subscribeManager.addListener(new SubscribeListener() {
            @Override
            public void onSubscribe(ExchangeProtocol exchangeProtocol) {
                System.out.println("B>>> " + exchangeProtocol);
            }
        });
        subscribeManager.onSubscribe(ExchangeProtocol.ack(1));
        subscribeManager.onSubscribe(ExchangeProtocol.ping(2));
    }

}