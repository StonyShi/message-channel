package com.stony.mc;

import junit.textui.TestRunner;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午11:36
 * @since 2019/1/15
 */
@RunWith(MethodOrderRunner.class)
public class SuiteTest {

//    public static void main(String[] args) throws Exception {
//
//        ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(10);
//
//        executorService.shutdown();
//        if(!executorService.awaitTermination(1, TimeUnit.HOURS)) {
//            executorService.shutdownNow();
//        }
//        System.out.println("------ 关闭 ---");
//    }


    @Test
    @MethodOrder(1)
    public void test_master() throws Exception {
        new SessionTest().test_master();
    }

    @Test
    @MethodOrder(2)
    public void test_worker_01() throws Exception {
        new SessionTest().test_worker();;
    }


    @Test
    @MethodOrder(3)
    public void test_consumer_01() throws Exception {
        new SessionTest().test_consumer("my", "ddx");
    }

    @Test
    @MethodOrder(3)
    public void test_consumer_02() throws Exception {
        new SessionTest().test_consumer("my", "ddx");
    }
    @Test
    @MethodOrder(3)
    public void test_consumer_03() throws Exception {
        new SessionTest().test_consumer("my2", "ddx");
    }

    @Test
    @MethodOrder(4)
    public void test_app_01() throws Exception {
        new SessionTest().test_producer(10000);
    }


    @Test
    @MethodOrder(4)
    public void test_app_02() throws Exception {
        new SessionTest().test_producer(10000);
    }


    @Test
    @MethodOrder(5)
    public void test_app_03() throws Exception {
        new SessionTest().test_producer(10000);
    }
}
