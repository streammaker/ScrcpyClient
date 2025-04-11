package com.example.scrcpyclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.scrcpyclient.connection.ClientSocketThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button connect;
    private EditText ipText;
    private EditText portText;
    private TextView receiveData;
    private SurfaceView surfaceView;
    private static final int DEVICE_NAME_REPLY = 1;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == DEVICE_NAME_REPLY) {
                    String deviceName = (String) msg.obj;
                    receiveData.setText(deviceName);
                }
            }
        };
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientSocketThread clientSocketThread = new ClientSocketThread(ipText.getText().toString(), Integer.valueOf(portText.getText().toString()), handler);
                clientSocketThread.initComponent(surfaceView);
                clientSocketThread.start();
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