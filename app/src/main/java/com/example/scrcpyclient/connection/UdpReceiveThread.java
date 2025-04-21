package com.example.scrcpyclient.connection;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.example.scrcpyclient.capture.ScreenCaptureService;
import com.example.scrcpyclient.util.Constant;
import com.example.scrcpyclient.util.Util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpReceiveThread extends Thread {
    private static final String TAG = UdpReceiveThread.class.getSimpleName();
    private volatile boolean isRunning = true;
    private Context context;
    private ActivityResultLauncher launcher;
    private MediaProjectionManager mediaProjectionManager;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public UdpReceiveThread(Context context, ActivityResultLauncher launcher) {
        this.context = context;
        this.launcher = launcher;
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(Constant.UDP_RECEIVE_PORT);
            while (isRunning) {
                byte[] container = new byte[1024];
                datagramPacket = new DatagramPacket(container, container.length);
                datagramSocket.receive(datagramPacket);
                byte[] data = datagramPacket.getData();
                int len = datagramPacket.getLength();
                checkData(data, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseResource();
        }
    }

    private void checkData(byte[] data, int length) {
        if (data[0] == 0x07 && data[length - 1] == 0x07) {
            Log.d(TAG, "UdpReceiveThread 数据包检查正确");
            byte[] realData = Arrays.copyOfRange(data, 1, length - 1);
            onReceive(realData, length - 2);
        } else {
            Log.d(TAG, "UdpReceiveThread 数据包检查错误");
        }
    }
    private void onReceive(byte[] data, int length) {
        int position = 0;
        if (data[position++] == 0x01) {
            byte content = data[position++];
            if (content == 0x01) {
                //开启屏幕捕获
                Log.d(TAG, "开启屏幕捕获");
                mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                launcher.launch(mediaProjectionManager.createScreenCaptureIntent());
            } else if (content == 0x02) {
                //停止屏幕捕获
                Log.d(TAG, "停止屏幕捕获");
                if (Util.isServiceRunning(context, Constant.SCREENCAPTURESERVICE)) {
                    Intent intent = new Intent(context, ScreenCaptureService.class);
                    context.stopService(intent);
                }
            }
        }
    }

    public void stopRunning() {
        isRunning = false;
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
