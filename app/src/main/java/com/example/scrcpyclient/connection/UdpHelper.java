package com.example.scrcpyclient.connection;

import android.content.Context;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

public class UdpHelper {
    private static final String TAG = UdpHelper.class.getSimpleName();

    private String ip;
    private Context context;
    private ActivityResultLauncher launcher;
    private UdpReceiveThread udpReceiveThread;
    private UdpSendThread udpSendThread;

    public UdpHelper(String ip, Context context, ActivityResultLauncher launcher) {
        this.ip = ip;
        this.context = context;
        this.launcher = launcher;
    }

    public void init() {
        udpSendThread = new UdpSendThread(ip);
        udpSendThread.start();
        udpReceiveThread = new UdpReceiveThread(context, launcher);
        udpReceiveThread.start();
    }

    public void releaseResource() {
        try {
            if (udpSendThread != null) {
                udpSendThread.stopRunning();
                udpSendThread.join();
                udpSendThread = null;
                Log.d(TAG, "udpSendThread releaseResource()");
            }
            if (udpReceiveThread != null) {
                udpReceiveThread.stopRunning();
                udpReceiveThread.join();
                udpReceiveThread = null;
                Log.d(TAG, "udpReceiveThread releaseResource()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
