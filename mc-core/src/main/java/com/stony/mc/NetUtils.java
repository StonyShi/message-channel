package com.stony.mc;

import com.stony.mc.session.HostPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.MacAddressUtil;

import java.net.*;
import java.util.Enumeration;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 上午10:27
 * @since 2019/1/17
 */
public abstract class NetUtils {

    private static final byte[] MACHINE_ID_BYTE;
    private static final String MACHINE_ID;
    static {
        MACHINE_ID_BYTE = MacAddressUtil.defaultMachineId();
        MACHINE_ID = MacAddressUtil.formatAddress(MACHINE_ID_BYTE);
    }
    public static String getMachineId() {
        return MACHINE_ID;
    }
    public static byte[] getMachineIdByte() {
        return MACHINE_ID_BYTE;
    }
    public static long getWorkId() {
        long workerId = 0;
        String wid = System.getProperty("mc.work.id");
        if (Utils.isNotEmpty(wid)) {
            try {
                workerId =  Long.valueOf(wid);
            } catch (Throwable e) {
                workerId = -1;
            }
        }
        if(workerId == -1) {
            char[] chars = getMachineId().replace(":", "").toCharArray();
            for (int i = chars.length - 1; i > chars.length - 7; i--) {
                workerId += chars[i] & 0x7f;
            }
        }
        return workerId;
    }
    public static HostPort getChannelHostPort(ChannelHandlerContext ctx) {
        return getChannelHostPort(ctx.channel());
    }
    public static HostPort getChannelHostPort(Channel channel) {
        SocketAddress socketAddress = channel.remoteAddress();
        if(socketAddress instanceof InetSocketAddress) {
            InetSocketAddress _socketAddress = (InetSocketAddress) socketAddress;
            String host =  _socketAddress.isUnresolved() ? _socketAddress.getHostName() : _socketAddress.getHostString();
            if(Utils.isEmpty(host)) {
                host = _socketAddress.getHostString();
            }
            return HostPort.wrap(host, _socketAddress.getPort());
        }
        String s = socketAddress.toString();
        String[] ss = s.split("/");
        String adds = ss[1];
        String name = ss[0];
        if(!name.isEmpty()) {
            return HostPort.wrap(name, Integer.valueOf(adds.split(":")[1]));
        }
        String[] addss = adds.split(":");
        return HostPort.wrap(addss[0], Integer.valueOf(addss[1]));
    }
    public static String getMcServerName() {
        return System.getProperty("mc.server.name");
    }

    public static String getMcServerName(String serverName) {
        return Utils.isNotEmpty(serverName) ? serverName : getMcServerName();
    }

    public static String getServerName() {
        String name = System.getProperty("mc.server.name");
        if (Utils.isNotEmpty(name)) {
            return name;
        }
        return getLocalHostname();
    }
    public static String getLocalHostname() {
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException ev) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nic = interfaces.nextElement();
                    final Enumeration<InetAddress> addresses = nic.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            final String hostname = address.getHostName();
                            if (hostname != null) {
                                return hostname;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                System.out.println(String.format("Could not determine local host name, %s",e.getMessage()));
                return "UNKNOWN_LOCALHOST";
            }
            System.out.println(String.format("Could not determine local host name, %s", ev.getMessage()));
            return "UNKNOWN_LOCALHOST";
        }
    }
}
