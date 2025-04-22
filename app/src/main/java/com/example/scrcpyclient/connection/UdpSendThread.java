package com.example.scrcpyclient.connection;

import android.util.Log;

import com.example.scrcpyclient.ClientMainActivity;
import com.example.scrcpyclient.device.Device;
import com.example.scrcpyclient.util.Constant;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UdpSendThread extends Thread {

    private static final String TAG = UdpSendThread.class.getSimpleName();
    private String ip;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public UdpSendThread(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket();
            byte[] data = Device.getDeviceName().getBytes();
            datagramPacket = new DatagramPacket(data, 0, data.length, InetAddress.getByName(ip), Constant.UDP_SEND_PORT);
            datagramSocket.send(datagramPacket);
            Log.d(TAG, "UdpSendThread 发送设备名字成功");

            //udp传输测试
            DatagramSocket datagramSocket1 = new DatagramSocket(Constant.UDP_RECEIVE_PORT);
            byte[] container = new byte[1024];
            DatagramPacket datagramPacket1 = new DatagramPacket(container, container.length);
            datagramSocket1.receive(datagramPacket1);
            byte[] data1 = datagramPacket1.getData();
            int len = datagramPacket1.getLength();
            String msg = new String(data1, 0, len);
            Log.d(TAG, "receive data : " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseResource();
        }
    }

    public void stopRunning() {
        releaseResource();
    }

    private void releaseResource() {
        Log.d(TAG, "releaseResource()");
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

}
