package com.example.scrcpyclient.connection;

import android.content.Context;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import java.io.OutputStream;

public class TcpHelper {
    private static final String TAG = TcpHelper.class.getSimpleName();

    private String ip;
    private Context context;
    private ActivityResultLauncher launcher;
    private TcpContactThread tcpContactThread;
    private TcpVideoThread tcpVideoThread;
    public static OutputStream videoOutputStream;

    public TcpHelper(String ip, Context context, ActivityResultLauncher launcher) {
        this.ip = ip;
        this.context = context;
        this.launcher = launcher;
    }

    public void init() {
        tcpContactThread = new TcpContactThread(ip, context, launcher);
        tcpContactThread.start();
        tcpVideoThread = new TcpVideoThread(ip, context);
        tcpVideoThread.start();
    }

    public void releaseResource() {
        try {
            if (tcpContactThread != null) {
                tcpContactThread.stopRunning();
                tcpContactThread.join();
                tcpContactThread = null;
                Log.d(TAG, "tcpContactThread releaseResource()");
            }
            if (tcpVideoThread != null) {
                tcpVideoThread.stopRunning();
                tcpVideoThread.join();
                tcpVideoThread = null;
                Log.d(TAG, "tcpVideoThread releaseResource()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveServerInfo(OutputStream outputStream) {
        videoOutputStream = outputStream;
    }

    public static void releaseStreamResource() {
        try {
            if (videoOutputStream != null) {
                videoOutputStream.close();
                videoOutputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
