package com.stony.mc.metrics;

import com.stony.mc.NetUtils;
import com.stony.mc.manager.ServerInfo;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>mc-core
 * <p>com.stony.mc.metrics
 *
 * @author stony
 * @version 上午10:53
 * @since 2019/1/14
 */
public class MetricStatistics implements MetricEventListener {

    AtomicLong connectionCount = new AtomicLong();
    AtomicLong connectionErrorCount = new AtomicLong();
    AtomicLong discardConnectionCount = new AtomicLong();
    AtomicLong maxConnectionCount = new AtomicLong();


    AtomicLong requestTotalCount = new AtomicLong();
    AtomicLong requestTotalBytes = new AtomicLong();

    AtomicLong responseTotalCount = new AtomicLong();
    AtomicLong responseTotalSucceedCount = new AtomicLong();
    AtomicLong responseTotalFailedCount = new AtomicLong();
    AtomicLong responseTotalTimeMS = new AtomicLong();  //milliseconds
    AtomicLong responseTotalBytes = new AtomicLong();

    AtomicLong subscriber = new AtomicLong();
    AtomicLong subscriberMessage = new AtomicLong();

    private final String machineId = NetUtils.getMachineId();


    public ServerInfo getServerInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setMachineId(machineId);
        serverInfo.setConnectionCount(connectionCount.get());
        serverInfo.setConnectionErrorCount(connectionErrorCount.get());
        serverInfo.setMaxConnectionCount(maxConnectionCount.get());
        serverInfo.setRequestTotalCount(requestTotalCount.get());
        return serverInfo;
    }

    public static String toString(String str) {
        return str == null ? "''" : String.format("'%s'", str);
    }
    public static String getEnvironment() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("{")
                .append("'java_vm_name':").append(toString(System.getProperty("java.vm.name"))).append(",")
                .append("'java_version':").append(toString(System.getProperty("java.version"))).append(",")
                .append("'os_arch':").append(toString(System.getProperty("os.arch"))).append(",")
                .append("'os_name':").append(toString(System.getProperty("os.name"))).append(",")
                .append("'os_version':").append(toString(System.getProperty("os.version")))
                .append("}");
        return builder.toString();
    }
    public String getStatisticsJson(String host, int port) {
        StringBuilder builder = new StringBuilder(512);
        builder.append("{")
                .append("machine_id:'").append(machineId).append("',")
                .append("server_name:").append(toString(host)).append(",")
                .append("server_port:").append(port).append(",")
                .append("connection_count:").append(connectionCount.get()).append(",")
                .append("max_connection_count:").append(maxConnectionCount.get()).append(",")
                .append("connection_error_count:").append(connectionErrorCount.get()).append(",")
                .append("discard_connection_ount:").append(discardConnectionCount.get()).append(",")
                .append("request_total_count:").append(requestTotalCount.get()).append(",")
                .append("request_total_bytes:").append(requestTotalBytes.get()).append(",")
                .append("response_total_count:").append(responseTotalCount.get()).append(",")
                .append("response_total_succeed_count:").append(responseTotalSucceedCount.get()).append(",")
                .append("response_total_failed_count:").append(responseTotalFailedCount.get()).append(",")
                .append("response_total_time_ms:").append(responseTotalTimeMS.get()).append(",")
                .append("response_total_bytes:").append(responseTotalBytes.get()).append(",")
                .append("subscriber_count:").append(subscriber.get()).append(",")
                .append("subscriber_message_count:").append(subscriberMessage.get()).append(",")
//                .append("env:").append(getEnvironment()).append(",")
                .append("created_time:").append(System.currentTimeMillis())
                .append("}");
        return builder.toString();
    }
    public String getStatisticsJson(int port) {
        return getStatisticsJson(null, port);
    }

    @Override
    public void onEvent(MetricEvent event, Duration duration, Long value) {
        switch (event) {
            case CONNECTION:
                connectionCount.incrementAndGet();
                maxConnectionCount.incrementAndGet();
                break;
            case DISCONNECT:
                connectionCount.decrementAndGet();
                break;
            case CONNECTION_ERROR:
                connectionErrorCount.incrementAndGet();
                connectionCount.decrementAndGet();
                break;
            case DISCARD_CONNECTION:
                discardConnectionCount.incrementAndGet();
//                connectionCount.decrementAndGet();
                break;
            case REQUEST:
                requestTotalCount.incrementAndGet();
                break;
            case REQUEST_BYTE_SIZE:
                requestTotalBytes.addAndGet(value);
                break;
            case REQUEST_SUCCEED:
                break;
            case REQUEST_FAILED:
                break;
            case RESPONSE:
                responseTotalCount.incrementAndGet();
                break;
            case RESPONSE_BYTE_SIZE:
                responseTotalBytes.addAndGet(value);
                break;
            case RESPONSE_SUCCEED:
                responseTotalSucceedCount.incrementAndGet();
                break;
            case RESPONSE_FAILED:
                responseTotalFailedCount.incrementAndGet();
                break;
            case RESPONSE_TIME_MS:
                responseTotalTimeMS.addAndGet(duration.toMillis());
                break;
            case SUBSCRIBER:
                subscriber.incrementAndGet();
                break;
            case SUBSCRIBED_MESSAGE:
                subscriberMessage.incrementAndGet();
                break;
            case SUBSCRIBED_MESSAGE_SUCCEED:
                break;
            case SUBSCRIBED_MESSAGE_FAILED:
                break;
            case UNSUBSCRIBE:
                subscriber.decrementAndGet();
                break;
            default:
                System.out.println("Not fount metric event.");
                break;
        }
    }
}