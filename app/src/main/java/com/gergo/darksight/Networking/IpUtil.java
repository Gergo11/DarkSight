package com.gergo.darksight.Networking;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class IpUtil {

    private Activity act;
    private static IpUtil ipUtil;

    private IpUtil(){}

    public String getWifiIP() {
        if (act != null) {
            try {
                WifiManager wifiManager = (WifiManager) act.getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                        (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return null;
            }
        }
        return null;
    }
    public String getMobileIP() {
        if (act != null) {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = (NetworkInterface) en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public int getNetworkType (){
        ConnectivityManager manager = (ConnectivityManager)act.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        boolean isMobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        if (isMobile){
            return 1;
        }
        if(isWifi){
            return 2;
        }
        return 0;
    }

    public void setAct(Activity act) {
        this.act = act;
    }

    public static IpUtil getIpUtil() {
        if(ipUtil == null){ ipUtil = new IpUtil();}
        return ipUtil;
    }
}
