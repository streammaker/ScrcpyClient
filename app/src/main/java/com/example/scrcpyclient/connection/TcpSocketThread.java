package com.example.scrcpyclient.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.scrcpyclient.capture.ScreenCaptureService;
import com.example.scrcpyclient.util.Constant;
import com.example.scrcpyclient.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TcpSocketThread extends Thread {
    private static final String TAG = TcpSocketThread.class.getSimpleName();

    private String ip;
    private Context context;
    private Handler handler;
    private Socket socket;
    private OutputStream videoOutputStream;

    public TcpSocketThread(String ip, Context context) {
        this.ip = ip;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            socket = new Socket(ip, Constant.TCP_SEND_PORT);
            videoOutputStream = socket.getOutputStream();
            TcpHelper.saveServerInfo(videoOutputStream);

            //tcp传输测试
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            Log.d(TAG, "tcp传输测试");
            if ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "data : " + line);
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(videoOutputStream));
            bufferedWriter.write("这是客户端发来的tcp测试数据");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        if (handler != null) {
            handler.post(() -> {
                releaseResource();
                if (Looper.myLooper() != null) {
                    Looper.myLooper().quit();
                }
            });
        }
    }

    private void releaseResource() {
        try {
            Log.d(TAG, "releaseResource()");
            //关服务处理资源释放
            if (Util.isServiceRunning(context, Constant.SCREENCAPTURESERVICE)) {
                Intent intent = new Intent(context, ScreenCaptureService.class);
                context.stopService(intent);
            }
            //关流
            TcpHelper.releaseStreamResource();
            if (videoOutputStream != null) {
                videoOutputStream.close();
                videoOutputStream = null;
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
