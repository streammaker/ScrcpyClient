package com.example.scrcpyclient.capture;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.example.scrcpyclient.MyApplication;
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
//    private DataOutputStream dos;
    private volatile boolean isReleased = false;
    private DataTransmitThread dataTransmitThread;

    public ScreenCaptureThread(MediaProjection mediaProjection, int density) {
        this.mediaProjection = mediaProjection;
        this.density = density;
    }

    @Override
    public void run() {
        Log.d(TAG, "run 111 : " + Thread.currentThread().getName());
//        dos = new DataOutputStream(TcpHelper.videoOutputStream);

        dataTransmitThread = new DataTransmitThread();
        dataTransmitThread.start();

        //不设置子线程的looper的话encoder.setCallbackd设置的回调会绑定到主线程的looper,导致回调在主线程运行，网络请求出错
        Looper.prepare();
        handler = new Handler(Looper.myLooper());
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d("NetCallback", "网络可用: " + network);
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.d("NetCallback", "网络断开: " + network);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                Log.d("NetCallback", "网络能力变化: " + networkCapabilities.toString());
                int down = networkCapabilities.getLinkDownstreamBandwidthKbps();
                int up = networkCapabilities.getLinkUpstreamBandwidthKbps();
                Log.d("NetCallback", "下行: " + down + "kbps, 上行: " + up + "kbps");
            }

            @Override
            public void onUnavailable() {
                Log.d("NetCallback", "网络不可用");
            }
        });
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
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
//        }

        //扩展选项
//        可变比特率
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
//        恒定比特率
//        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
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
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                    Log.d(TAG, "关键帧出现，时间戳: " + bufferInfo.presentationTimeUs);
                }
                if (!isReleased) {
                    sendEncodedData(i, bufferInfo);
                }
            }

            @Override
            public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
                //扩展选项
                Log.d(TAG, "luozhenfeng " + "onOutputFormatChanged" + Thread.currentThread().getName());
                int actualBitrate1 = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
                Log.d(TAG, "实际码率 : " + actualBitrate1);
                if (mediaFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
                    int actualBitrate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
                    Log.d(TAG, "实际码率 : " + actualBitrate);
                }
            }
        };
    }

    private void sendEncodedData(int index, MediaCodec.BufferInfo info) {
//        Log.d(TAG, "sendEncodedData : " + Thread.currentThread().getName());
        ByteBuffer buffer = encoder.getOutputBuffer(index);
        if (buffer == null) {
            Log.d(TAG, "buffer == null");
            encoder.releaseOutputBuffer(index, false);
            return;
        }

        byte[] packet = new byte[info.size];
        buffer.get(packet);


        //需要在线程中发送数据，否在当write阻塞的时候会导致编码器也阻塞，
        //再一直传数据到编码器的话视频帧数据会拿不到编码器缓冲区导致编码器异常然后设备重启
        try {
            if (dataTransmitThread != null) {
                dataTransmitThread.sendEncodedData(index, packet);
            }
//            dos.writeInt(packet.length);
//            Log.d(TAG, index + "---" + packet.length);
//            dos.write(packet);
//            dos.flush();
//            Log.d(TAG, index + "---" + info.size);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encoder.releaseOutputBuffer(index, false);
        }
    }

    public void updateBitRate(int newBitRate) {
        try {
            if (encoder != null) {
                MediaCodecInfo.CodecCapabilities caps = encoder.getCodecInfo().getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC);

                //扩展选项
                if (caps.getEncoderCapabilities().isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)) {
                    Log.d(TAG, "编码器支持可变码率模式");
                }

                Bundle params = new Bundle();
                params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newBitRate);
                encoder.setParameters(params);
                //请求关键帧使新码率立即生效（可选）
                requestKeyFrame();
            }
        } catch (IllegalStateException e) {
            Log.d(TAG, "falied to update bitrate !!!", e);
        }
    }

    private void requestKeyFrame() {
        try {
            if (encoder != null) {
                Bundle params = new Bundle();
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                encoder.setParameters(params);
            }
        } catch (IllegalStateException e) {
            Log.d(TAG, "failed to request key frame !!!", e);
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
            //待处理
//            if (dos != null) {
//                dos.close();
//                dos = null;
//            }
            if (dataTransmitThread != null) {
                dataTransmitThread.quit();
                try {
                    dataTransmitThread.join();
                    Log.d(TAG, "dataTransmitThread.join() !!!");
                    dataTransmitThread = null;
                } catch (Exception e) {
                    Log.d(TAG, "dataTransmitThread.join() error !!!");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
