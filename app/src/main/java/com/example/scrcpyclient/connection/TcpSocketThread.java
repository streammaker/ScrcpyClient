package com.example.scrcpyclient.connection;

import android.os.Handler;
import android.os.Looper;

import com.example.scrcpyclient.util.Constant;

import java.io.OutputStream;
import java.net.Socket;

public class TcpSocketThread extends Thread {

    private String ip;
    private Handler handler;

    public TcpSocketThread(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            Socket socket = new Socket(ip, Constant.TCP_SEND_PORT);
            OutputStream videoOutputStream = socket.getOutputStream();
            TcpHelper.saveServerInfo(videoOutputStream);
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
