package com.hikdsj.hikdsj;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/********************************************************************
 @version: 1.0.0
 @description: 获取设备信息
 @author: 杨帆
 @time: 2022-03-21 14:51
 @变更历史:
 ********************************************************************/
public class DeviceUtil {
    private volatile static DeviceUtil instance;

    private DeviceUtil() {
    }

    public static DeviceUtil getInstance() {
        instance = new DeviceUtil();
        return instance;
    }

    /**
     * 获取设备ip
     *
     * @return  设备ip
     */
    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
}
