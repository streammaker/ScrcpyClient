package com.example.scrcpyclient.capture;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.scrcpyclient.connection.TcpHelper;

import java.io.DataOutputStream;

public class DataTransmitThread extends Thread {

    private static final String TAG = DataTransmitThread.class.getSimpleName();
    private Handler handler;
    private DataOutputStream dos;

    @Override
    public void run() {
        try {
            dos = new DataOutputStream(TcpHelper.videoOutputStream);
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "run() releaseResource");
            releaseResource();
        }
    }

    public void sendEncodedData(int index, byte[] data) {
        if (handler != null) {
            handler.post(() -> {
                try {
                    dos.writeInt(data.length);
                    Log.d(TAG, index + "---" + data.length);
                    dos.write(data);
                    dos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void quit() {
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
//            if (dos != null) {
//                dos.close();
//                dos = null;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
