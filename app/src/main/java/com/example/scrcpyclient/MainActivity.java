package com.example.scrcpyclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scrcpyclient.connection.ClientSocketThread;
import com.example.scrcpyclient.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private Button connect;
    private EditText ipText;
    private EditText portText;
    private TextView receiveData;
    private SurfaceView surfaceView;
    private ClientSocketThread clientSocketThread;


    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        init();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == Constant.DEVICE_NAME_REPLY) {
                    String deviceName = (String) msg.obj;
                    receiveData.setText(deviceName);
                } else if (msg.what == Constant.DISCONNECT_SERVER) {
                    receiveData.setText("disconnect server");
                }
            }
        };
        connect.setOnClickListener(view -> {
            if (clientSocketThread == null || !clientSocketThread.isAlive()) {
                Log.d(TAG, "start clientSocketThread");
                clientSocketThread = new ClientSocketThread(ipText.getText().toString(), Integer.valueOf(portText.getText().toString()), handler);
                clientSocketThread.initComponent(context, surfaceView);
                clientSocketThread.start();
            } else {
                Toast.makeText(context, "当前已连接服务端", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "has connected to server");
            }
        });
    }

    private void init() {
        connect = findViewById(R.id.connect);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
        receiveData = findViewById(R.id.receiveData);
        surfaceView = findViewById(R.id.surfaceView);
    }
}