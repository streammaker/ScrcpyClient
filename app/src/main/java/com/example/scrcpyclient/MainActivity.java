package com.example.scrcpyclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientSocketThread clientSocketThread = new ClientSocketThread(ipText.getText().toString(), Integer.valueOf(portText.getText().toString()));
                clientSocketThread.start();
            }
        });
    }

    private void init() {
        connect = findViewById(R.id.connect);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
        receiveData = findViewById(R.id.receiveData);
    }
}