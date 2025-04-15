package com.example.scrcpyclient.connection;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

import com.example.scrcpyclient.util.Constant;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientSocketThread extends Thread {

    private static final String TAG = ClientSocketThread.class.getSimpleName();
    private Context context;
    private final String ip;
    private final int port;
    private Handler handler;
    private SurfaceView surfaceView;
    // 解码器
    private MediaCodec mDecoder;
    DataInputStream dis;
    private volatile boolean mIsReceiving = true;
    ClientSocketHelper connection;
    public ClientSocketThread(String ip, int port, Handler handler) {
        this.ip = ip;
        this.port = port;
        this.handler = handler;
    }
    @Override
    public void run() {
        try {
            connection = ClientSocketHelper.connect(ip, port);
            byte[] buffer = new byte[1024];
            int bytesRead = ClientSocketHelper.videoInputStream.read(buffer);
            String deviceName = new String(buffer, 0, bytesRead);
            Log.d(TAG, "receive data : " + deviceName);
            Message message = new Message();
            message.what = Constant.DEVICE_NAME_REPLY;
            message.obj = deviceName;
            handler.sendMessage(message);
            dis = new DataInputStream(ClientSocketHelper.videoInputStream);

            initializeDecoder();

            while (mIsReceiving && !connection.isClosed()) {
                Log.d(TAG, "prepare receive video data");
                processNetworkPacket();
            }


//            while ((bytesRead = ClientSocketHelper.videoInputStream.read(buffer)) != 0) {
//                String deviceName = new String(buffer, 0, bytesRead);
//                Log.d(TAG, "receive data : " + deviceName);
//            }
        } catch (IOException e) {
            Message message = new Message();
            message.what = Constant.DISCONNECT_SERVER;
            handler.sendMessage(message);
            releaseDecoder();
            ClientSocketHelper.releaseResource();
            e.printStackTrace();
        }
    }

    private void initializeDecoder() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            mDecoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mDecoder.configure(format, surfaceView.getHolder().getSurface(), null, 0);
            mDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processNetworkPacket() throws IOException {
        int packetSize = dis.readInt();
        if (packetSize <= 0) return;
        byte[] frameData = new byte[packetSize];
        dis.readFully(frameData, 0, packetSize);
        feedDataToDecoder(frameData);
    }

    private void feedDataToDecoder(byte[] data) {
        if (mDecoder == null) return;
        try {
            int inputBufferIndex = mDecoder.dequeueInputBuffer(Constant.DECODER_TIMEOUT_US);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferIndex);
                inputBuffer.put(data);
                mDecoder.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        data.length,
                        System.nanoTime() / 1000,
                        0
                );
                renderDecodedFrames();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderDecodedFrames() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex;
        while ((outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, Constant.DECODER_TIMEOUT_US)) >= 0) {
            mDecoder.releaseOutputBuffer(outputBufferIndex, true);
        }
    }

    public void initComponent(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
    }

    private void releaseDecoder() {
        try {
            mIsReceiving = false;
            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();
                mDecoder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
