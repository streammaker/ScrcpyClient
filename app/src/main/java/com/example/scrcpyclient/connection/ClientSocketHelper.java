package com.example.scrcpyclient.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSocketHelper {

    private static final String TAG = ClientSocketHelper.class.getSimpleName();
    private static Socket videoSocket;
    public static InputStream videoInputStream;
    public static OutputStream videoOutputStream;

    public ClientSocketHelper(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }
    public static ClientSocketHelper connect(String ip, int port) throws IOException {
        Socket videoSocket = new Socket(ip, port);
        videoInputStream = videoSocket.getInputStream();
        videoOutputStream = videoSocket.getOutputStream();
        return new ClientSocketHelper(videoSocket);
    }
    public Boolean isClosed() {
        return videoSocket.isClosed();
    }

    public static void releaseResource() {
        try {
            Log.d(TAG, "releaseResource");
            if (videoInputStream != null) {
                videoInputStream.close();
                videoInputStream = null;
            }
            if (videoOutputStream != null) {
                videoOutputStream.close();
                videoOutputStream = null;
            }
            if (videoSocket != null && !videoSocket.isClosed()) {
                videoSocket.close();
                videoSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
