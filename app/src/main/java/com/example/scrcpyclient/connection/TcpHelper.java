package com.example.scrcpyclient.connection;

import java.io.OutputStream;

public class TcpHelper {

    private String ip;
    private TcpSocketThread tcpSocketThread;
    public static OutputStream videoOutputStream;

    public TcpHelper(String ip) {
        this.ip = ip;
    }

    public void init() {
        tcpSocketThread = new TcpSocketThread(ip);
        tcpSocketThread.start();
    }

    public static void saveServerInfo(OutputStream outputStream) {
        videoOutputStream = outputStream;
    }

}
