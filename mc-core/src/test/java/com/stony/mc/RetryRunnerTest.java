package com.stony.mc;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.manager.ServerInfo;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午2:52
 * @since 2019/1/15
 */
@RunWith(RetryRunner.class)
public class RetryRunnerTest {

    @Test
    @RetryRunner.Repeat(3)
    public void test_17(){
        System.out.println("77777777");
    }

    @Ignore
    @Test
    @RetryRunner.Repeat(3)
    public void test_18(){
        System.out.println("888888");
    }


    @Test
    public void test_33(){
        String s = "{machine_id:'c4:b3:01:ff:fe:d0:59:e1',server_port:4091,connection_count:-3,connection_error_count:0,max_connection_count:6,request_total_count:30006,request_total_bytes:1433542,response_total_count:90006,response_total_succeed_count:90006,response_total_failed_count:0,response_total_time_ms:10645833,response_total_bytes:3376782,subscriber_count:0,subscriber_message_count:0,created_time:1547538730992}";

//        JSONObject o = JSONObject.parseObject(s);
//        o.entrySet().stream().forEach(kv -> {
//
//            System.out.println(String.format("private long %s;", kv.getKey()));
//        });

        ServerInfo v = JSONObject.parseObject(s, ServerInfo.class);
        System.out.println(v);
    }

    @Test
    public void test_45() {
        Class clazz = ServerInfo.class;
        for (Field f : clazz.getDeclaredFields()) {
            System.out.println(String.format("@JSONField(name = \"%s\")", f.getName()));
            System.out.println(String.format("private %s %s;", f.getType().getSimpleName(), Utils.toCamel2(f.getName())));
//            System.out.println(f);
        }
    }

    @Test
    public void test_62(){
        System.out.println(Arrays.toString(normalization(new long[]{10L, 30L, 25L}, 1L, 10L)));
        System.out.println(Arrays.toString(normalization(new long[]{13L, 32L, 23L}, 1L, 10L)));
        System.out.println(Arrays.toString(normalization(new long[]{15L, 35L, 29L}, 1L, 10L)));
        System.out.println(Arrays.toString(normalization(new long[]{5L, 15L, 9L}, 1L, 10L)));
    }

    long[] normalization(long[] x, long ymin, long ymax) {
        long xmin = Arrays.stream(x).min().getAsLong();
        long xmax = Arrays.stream(x).max().getAsLong();
        return Arrays.stream(x).map(_x -> (Math.round((ymax - ymin) * (_x - xmin) / (xmax - xmin) + ymin))).toArray();
    }





}