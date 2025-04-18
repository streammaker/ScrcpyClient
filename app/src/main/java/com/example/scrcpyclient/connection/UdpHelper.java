package com.example.scrcpyclient.connection;

import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;

public class UdpHelper {

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

}
