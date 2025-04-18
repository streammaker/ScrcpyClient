package com.example.scrcpyclient.capture;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.scrcpyclient.ClientMainActivity;
import com.example.scrcpyclient.R;
import com.example.scrcpyclient.util.Constant;

public class ScreenCaptureService extends Service  {

    private static final String TAG = ScreenCaptureService.class.getSimpleName();
    private MediaProjection mediaProjection;
    private ScreenCaptureThread screenCaptureThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate !!!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand !!!");
        if (intent.getAction().equals("ACTION_START_CAPTURE")) {
            createNotificationChannel();
            Notification notification = buildNotification();
            startForeground(Constant.NOTIFICATION_ID, notification);
            int resultCode = intent.getIntExtra("result_code", Activity.RESULT_CANCELED);
            Intent resultData = intent.getParcelableExtra("result_data");
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
            screenCaptureThread = new ScreenCaptureThread(mediaProjection, getResources().getDisplayMetrics().densityDpi);
            screenCaptureThread.start();
            Log.d("luozhenfeng", "1111");
            return START_STICKY;
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void createNotificationChannel() {
        NotificationManager mannager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constant.CHANNEL_ID,
                    "Screen Capture",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            mannager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, ClientMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                .setContentTitle("Screen Capture Active")
                .setContentText("Streaming device screen...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.large_icon))
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
