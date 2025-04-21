package com.example.scrcpyclient.capture;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.example.scrcpyclient.connection.TcpHelper;
import com.example.scrcpyclient.util.Constant;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenCaptureThread extends Thread {
    private static final String TAG = ScreenCaptureThread.class.getSimpleName();

    private Handler handler;
    private MediaProjection mediaProjection;
    int density;
    private MediaCodec encoder;
    private Surface encoderSurface;
    private VirtualDisplay virtualDisplay;
    private DataOutputStream dos;
    private volatile boolean isReleased = false;

    public ScreenCaptureThread(MediaProjection mediaProjection, int density) {
        this.mediaProjection = mediaProjection;
        this.density = density;
    }

    @Override
    public void run() {
        Log.d(TAG, "run 111 : " + Thread.currentThread().getName());
        dos = new DataOutputStream(TcpHelper.videoOutputStream);
        //不设置子线程的looper的话encoder.setCallbackd设置的回调会绑定到主线程的looper,导致回调在主线程运行，网络请求出错
        Looper.prepare();
        handler = new Handler(Looper.myLooper());
        startScreenCapture();
        Looper.loop();
        if (!isReleased) {
            releaseResource();
        }
        Log.d(TAG, "ScreenCaptureThread 结束");
    }

    public void startScreenCapture() {
        try {
            Log.d("luozhenfeng", "startScreenCapture : " + Thread.currentThread().getName());
            initVideoEncoder();
            createVirtualDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVideoEncoder() throws IOException {
        Log.d(TAG, "initVideoEncoder : " + Thread.currentThread().getName());
        MediaFormat format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
//        }
        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.setCallback(createEncoderCallback());
        encoderSurface = encoder.createInputSurface();
        encoder.start();
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCast",
                Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                encoderSurface, null, null);
    }

    private MediaCodec.Callback createEncoderCallback() {
        return new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {

            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
//                Log.d("luozhenfeng", "onOutputBufferAvailable : " + Thread.currentThread().getName());
                if (!isReleased) {
                    sendEncodedData(i, bufferInfo);
                }
            }

            @Override
            public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

            }
        };
    }

    private void sendEncodedData(int index, MediaCodec.BufferInfo info) {
        Log.d(TAG, "sendEncodedData : " + Thread.currentThread().getName());
        ByteBuffer buffer = encoder.getOutputBuffer(index);
        if (buffer == null) return;

        byte[] packet = new byte[info.size];
        buffer.get(packet);

        try {
            dos.writeInt(packet.length);
            dos.write(packet);
            dos.flush();
//            Log.d(TAG, index + "---" + info.size);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encoder.releaseOutputBuffer(index, false);
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
            Log.d(TAG, "releaseResource()");
            isReleased = true;
            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
            }
            if (encoder != null) {
                encoder.stop();
                encoder.release();
                encoder = null;
            }
            if (mediaProjection != null) {
                mediaProjection.stop();
                mediaProjection = null;
            }
//            if (dos != null) {
//                dos.close();
//                dos = null;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
