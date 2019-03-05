package com.stony.mc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.manager.ChannelContextHolder;
import com.stony.mc.session.HostPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午10:35
 * @since 2019/1/15
 */
public class IdTest {
    @Test
    public void test_idx(){
        IdGenerator idGenerator = SimpleIdGenerator.getInstance();
//        idGenerator = SnowflakeIdGenerator.getInstance();
        for (int i = 0; i < 10000; i++) {
            System.out.println(idGenerator.nextId());
        }
    }


    @Test
    public void test_id(){
        String s = "localhost/127.0.0.1:90";
        System.out.println(s.split("/")[1]);
        s = "/127.0.0.1:90";
        System.out.println(s.split("/")[1]);


        System.out.println(s.split("/")[0].isEmpty());

        System.out.println(Arrays.toString(s.split("/")));
    }

    @Test
    public void test_it(){
        System.out.println(Arrays.asList(1).iterator().next());
        System.out.println(Arrays.asList(1, 3, 2).iterator().next());
    }

    @Test
    public void test_map() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        System.out.println("- " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("> " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }, 10, 10, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            System.out.println("o " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }, 30, TimeUnit.SECONDS);
        scheduler.schedule(() -> {
            beeperHandle.cancel(true);
            latch.countDown();
            System.out.println("c " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }, 70, TimeUnit.SECONDS);

        latch.await();
        System.out.println("- " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        scheduler.shutdown();
    }


    @Test
    public void test_context() throws InterruptedException {
        ChannelContextHolder[] contextHolders = new ChannelContextHolder[3];
        contextHolders[0] = new ChannelContextHolder(new ChannelHandlerContextTv("A"));
        contextHolders[0].beginProcessing();
        Thread.sleep(1000L);

        contextHolders[1] = new ChannelContextHolder(new ChannelHandlerContextTv("B"));
        contextHolders[1].beginProcessing();
        Thread.sleep(1000L);

        contextHolders[2] = new ChannelContextHolder(new ChannelHandlerContextTv("C"));
        contextHolders[2].beginProcessing();
        Thread.sleep(1000L);

        contextHolders[0].endProcessing();

        Arrays.sort(contextHolders, new Comparator<ChannelContextHolder>() {
            @Override
            public int compare(ChannelContextHolder o1, ChannelContextHolder o2) {
                return o1.getLastProcessingTime().compareTo(o2.getLastProcessingTime());
            }
        });
        Assert.assertEquals(contextHolders[0].getCtx().toString(), "B");
        Assert.assertEquals(contextHolders[1].getCtx().toString(), "C");
        Assert.assertEquals(contextHolders[2].getCtx().toString(), "A");

        System.out.println(Arrays.toString(contextHolders));
    }

    class ChannelHandlerContextTv implements ChannelHandlerContext {
        final String name;

        ChannelHandlerContextTv(String name) {
            this.name = name;
        }

        @Override
        public Channel channel() {
            return null;
        }

        @Override
        public EventExecutor executor() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public ChannelHandler handler() {
            return null;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public ChannelHandlerContext fireChannelRegistered() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelUnregistered() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelActive() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelInactive() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireUserEventTriggered(Object evt) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelRead(Object msg) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelReadComplete() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelWritabilityChanged() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture disconnect() {
            return null;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public ChannelFuture deregister() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture disconnect(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture close(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture deregister(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelHandlerContext read() {
            return null;
        }

        @Override
        public ChannelFuture write(Object msg) {
            return null;
        }

        @Override
        public ChannelFuture write(Object msg, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelHandlerContext flush() {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg) {
            return null;
        }

        @Override
        public ChannelPromise newPromise() {
            return null;
        }

        @Override
        public ChannelProgressivePromise newProgressivePromise() {
            return null;
        }

        @Override
        public ChannelFuture newSucceededFuture() {
            return null;
        }

        @Override
        public ChannelFuture newFailedFuture(Throwable cause) {
            return null;
        }

        @Override
        public ChannelPromise voidPromise() {
            return null;
        }

        @Override
        public ChannelPipeline pipeline() {
            return null;
        }

        @Override
        public ByteBufAllocator alloc() {
            return null;
        }

        @Override
        public <T> Attribute<T> attr(AttributeKey<T> key) {
            return null;
        }

        @Override
        public <T> boolean hasAttr(AttributeKey<T> key) {
            return false;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}