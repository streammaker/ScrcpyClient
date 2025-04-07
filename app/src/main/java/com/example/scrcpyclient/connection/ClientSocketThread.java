package com.example.scrcpyclient.connection;

import android.util.Log;

import java.io.IOException;

public class ClientSocketThread extends Thread {

    private static final String TAG = ClientSocketThread.class.getSimpleName();
    private final String ip;
    private final int port;
    public ClientSocketThread(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    @Override
    public void run() {
        try {
            ClientSocketHelper connection = ClientSocketHelper.connect(ip, port);
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = ClientSocketHelper.videoInputStream.read(buffer)) != 0) {
                String deviceName = new String(buffer, 0, bytesRead);
                Log.d(TAG, "receive data : " + deviceName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
