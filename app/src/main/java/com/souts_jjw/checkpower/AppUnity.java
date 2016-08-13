package com.souts_jjw.checkpower;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AppUnity {
    /**
     * 檢查網路是否連線
     */
    private static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 判斷當前網路是否為wifi
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * wifi是否開啟
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 是否充電中
     */
    public static boolean isCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        return usbCharge || acCharge;
    }

    public static boolean Wait(long millis) {
        try {
            Thread.sleep(millis);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
