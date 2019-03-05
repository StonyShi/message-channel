package com.stony.mc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.manager.RegisterInfoFilter;
import com.stony.mc.manager.ServerEnvironment;
import com.stony.mc.manager.ServerInfo;
import com.stony.mc.metrics.MetricStatistics;
import com.stony.mc.session.HostPort;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午10:22
 * @since 2019/1/24
 */
public class JsonTest {

    @BeforeClass
    public static void beforeClass(){
        System.out.println(Utils.isNotEmpty("x"));
    }
    @Test
    public void test_json(){
        RegisterInfo info = new RegisterInfo("12345");
        info.setAddress("localhost:8011");
        info.setDevice("phone_123123123");
        info.setWorkerHost("localhost");
        info.setWorkerPort(8011);
        info.addTag("driver");
        info.addTag("beijing");
        info.addAlias("Luk");

        String str = JSONObject.toJSONString(info);
        System.out.println(str);

        System.out.println(JSONObject.parseObject(str, RegisterInfo.class));

        HashSet<String> mt = new HashSet<>();
        mt.add("driver");
        RegisterInfoFilter filter = new RegisterInfoFilter();
        filter.setMatchTags(mt);
        System.out.println("-------");
        long startTime = System.currentTimeMillis();
        System.out.println(filter.test(info));
        System.out.println("cost time: " + (System.currentTimeMillis()-startTime));
        Assert.assertEquals(true, filter.test(info));

        System.out.println("-------");
        filter.setUid("123");
        startTime = System.currentTimeMillis();
        System.out.println(filter.test(info));
        System.out.println("cost time: " + (System.currentTimeMillis()-startTime));
        Assert.assertEquals(false, filter.test(info));


        RegisterInfoFilter filter2 = new RegisterInfoFilter();
        filter2.setFilterTags(mt);
        Assert.assertEquals(false, filter2.test(info));


    }

    @Test
    public void test_host(){
        List<HostPort> list = new ArrayList<>();
        list.add(new HostPort("localhost", 91));
        list.add(new HostPort("localhost", 92));
        System.out.println(list.stream().map(v -> v.toJson()).collect(Collectors.joining(",", "[", "]")));
        System.out.println(JSONObject.toJSONString(list));
        list = JSONObject.parseObject(list.stream().map(v -> v.toJson()).collect(Collectors.joining(",", "[", "]")),
                new TypeReference<List<HostPort>>(){});
        System.out.println(list);

    }

    @Test
    public void test_server(){
        ServerInfo info = new ServerInfo();
        info.setServerName("localhost");
        info.setServerPort(9011);
        info.setMachineId("234-dfdfdxcvxv");
        String str = JSONObject.toJSONString(info);
        System.out.println(str);
        System.out.println(JSONObject.parseObject(str, ServerInfo.class));

    }

    @Test
    public void test_env(){
        // || key.startsWith("java.")
        //java.vm.name
        //java.version
        //java.vendor
//        key=os.arch, value=x86_64
//        key=os.name, value=Mac OS X
//        key=os.version, value=10.12

        System.out.println("---");
//        System.getProperties().entrySet().stream()
//                .filter(kv -> {
//                    String key = kv.getKey().toString();
//                    return key.startsWith("java.");
//                })
//                .forEach(kv -> {
//                    System.out.println(String.format("key=%s, value=%s", kv.getKey(), kv.getValue()));
//                });
        MetricStatistics s = new MetricStatistics();
        System.out.println(s.getStatisticsJson(88).length());
        System.out.println(JSONObject.parseObject(s.getEnvironment(), ServerEnvironment.class));

        System.out.println(JSONObject.parseObject(s.getStatisticsJson(99), ServerInfo.class));

    }
}