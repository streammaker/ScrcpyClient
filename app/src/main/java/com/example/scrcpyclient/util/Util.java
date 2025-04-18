package com.example.scrcpyclient.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.Iterator;
import java.util.List;

public class Util {

    public static boolean isServiceRunning(Context context, String serviceName) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (serviceList.size() <= 0) {
            return isRunning;
        }
        Iterator iter = serviceList.iterator();
        while (iter.hasNext()) {
            if (((ActivityManager.RunningServiceInfo) iter.next()).service.getClassName().equals(serviceName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
