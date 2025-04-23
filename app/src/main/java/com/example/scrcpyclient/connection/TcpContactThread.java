package com.example.scrcpyclient.connection;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.example.scrcpyclient.capture.ScreenCaptureService;
import com.example.scrcpyclient.device.Device;
import com.example.scrcpyclient.util.Constant;
import com.example.scrcpyclient.util.Util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

//接收并解析服务端数据
public class TcpContactThread extends Thread {
    private static final String TAG = TcpContactThread.class.getSimpleName();

    private String ip;
    private Context context;
    private ActivityResultLauncher launcher;
    private Socket socket;
    private InputStream contactInputStream;
    private OutputStream contactOutputStream;
    private volatile boolean isRunning = true;
    private MediaProjectionManager mediaProjectionManager;

    public TcpContactThread(String ip, Context context, ActivityResultLauncher launcher) {
        this.ip = ip;
        this.context = context;
        this.launcher = launcher;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, Constant.TCP_CONTACT_PORT);
            contactInputStream = socket.getInputStream();
            contactOutputStream = socket.getOutputStream();
            contactOutputStream.write(Device.getDeviceName().getBytes());
            Log.d(TAG, "发送客户端设备名称");
            while (isRunning) {
                byte[] receiveData = new byte[1024];
                int len = contactInputStream.read(receiveData);
                byte[] contentData = Arrays.copyOfRange(receiveData, 0, len);
                checkData(contentData, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "run() releaseResource");
            releaseResource();
        }
    }

    private void checkData(byte[] data, int length) {
        if (data[0] == 0x07 && data[length - 1] == 0x07) {
            Log.d(TAG, "TcpContactThread 数据包检查正确");
            byte[] realData = Arrays.copyOfRange(data, 1, length - 1);
            onReceive(realData, length - 2);
        } else {
            Log.d(TAG, "TcpContactThread 数据包检查错误");
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
        try {
            if (contactOutputStream != null) {
                contactOutputStream.close();
                contactOutputStream = null;
            }
            if (contactInputStream != null) {
                contactInputStream.close();
                contactInputStream = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
