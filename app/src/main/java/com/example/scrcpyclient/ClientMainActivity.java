package com.example.scrcpyclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.scrcpyclient.capture.ScreenCaptureService;
import com.example.scrcpyclient.connection.TcpHelper;
import com.example.scrcpyclient.util.Constant;
import com.example.scrcpyclient.util.Util;

public class ClientMainActivity extends AppCompatActivity {

    private static final String TAG = ClientMainActivity.class.getSimpleName();
    private Context context;
    private Button connect;
    private EditText ipText;
    private TcpHelper tcpHelper;
    private ActivityResultLauncher launcher;

    private Button low_bit;
    private Button middle_bit;
    private Button high_bit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_layout);
        context = this;
        init();
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (Util.isServiceRunning(context, Constant.SCREENCAPTURESERVICE)) {
                    Toast.makeText(context, "屏幕捕获服务已开启...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "screenCaptureService has started");
                } else {
                    Log.d(TAG, "开始屏幕捕获");
                    Intent serviceIntent = new Intent(context, ScreenCaptureService.class);
                    serviceIntent.setAction("ACTION_START_CAPTURE");
                    serviceIntent.putExtra("result_code", result.getResultCode());
                    serviceIntent.putExtra("result_data", result.getData());
                    startService(serviceIntent);
                }
            } else {
                Toast.makeText(context, "授权失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "授权失败");
            }
        });
        connect.setOnClickListener(view -> {
            String serverIP = ipText.getText().toString();
            tcpHelper = new TcpHelper(serverIP, context, launcher);
            tcpHelper.init();
        });
        low_bit.setOnClickListener(view -> {
            Intent intent = new Intent("ACTION_BITRATE_CHANGED");
            intent.putExtra("bit_rate", 100_000);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        });
        middle_bit.setOnClickListener(view -> {
            Intent intent = new Intent("ACTION_BITRATE_CHANGED");
            intent.putExtra("bit_rate", 2_000_000);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        });
        high_bit.setOnClickListener(view -> {
            Intent intent = new Intent("ACTION_BITRATE_CHANGED");
            intent.putExtra("bit_rate", 8_000_000);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        });
    }

    private void init() {
        connect = findViewById(R.id.connect);
        ipText = findViewById(R.id.ipText);

        low_bit = findViewById(R.id.low_bit);
        middle_bit = findViewById(R.id.middle_bit);
        high_bit = findViewById(R.id.high_bit);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

//        待处理，退出应用后仍然可以捕获屏幕
//        if (tcpHelper != null) {
//            tcpHelper.releaseResource();
//            tcpHelper = null;
//        }
    }
}