package com.example.scrcpyclient.connection;

import android.content.Context;
import android.util.Log;

import com.example.scrcpyclient.ClientMainActivity;

import java.io.OutputStream;

public class TcpHelper {
    private static final String TAG = TcpHelper.class.getSimpleName();

    private String ip;
    private Context context;
    private TcpSocketThread tcpSocketThread;
    public static OutputStream videoOutputStream;

    public TcpHelper(String ip, Context context) {
        this.ip = ip;
        this.context = context;
    }

    public void init() {
        tcpSocketThread = new TcpSocketThread(ip, context);
        tcpSocketThread.start();
    }

    public void releaseResource() {
        try {
            if (tcpSocketThread != null) {
                tcpSocketThread.stopRunning();
                tcpSocketThread.join();
                tcpSocketThread = null;
                Log.d(TAG, "tcpSocketThread releaseResource()");
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
