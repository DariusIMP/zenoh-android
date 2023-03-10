package com.example.zenoh;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ZenohdService extends Service {
    private static final String TAG = ZenohdService.class.getSimpleName();

    private static final String COMMAND = "./data/local/tmp/zenohd";

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "zenohd";
    private final IBinder binder = new ZenohdBinder();
    private HandlerThread handlerThread;
    private Handler handler;
    private boolean isServiceRunning = false;
    private Runnable runnable;

    private Process zenohdProcess;

    private MutableLiveData<String> zenohdLogs = new MutableLiveData<>();

    public MutableLiveData<String> getZenohdLogs() {
        return zenohdLogs;
    }

    public class ZenohdBinder extends Binder {
        ZenohdService getService() {
            return ZenohdService.this;
        }
    }

    private void executeZenohd() {
        try {
            zenohdProcess = Runtime.getRuntime().exec(COMMAND);
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(zenohdProcess.getInputStream())
            );
            String line = "";
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
                zenohdLogs.postValue(builder.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate() {
        handlerThread = new HandlerThread("ZenohdServiceThread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = getNotification();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Toast.makeText(this, "Zenohd service starting", Toast.LENGTH_SHORT).show();
        runnable = this::executeZenohd;
        handler.post(runnable);
        this.isServiceRunning = true;
        return START_STICKY;
    }

    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "zenohd", importance);
        channel.setDescription("zenohd channel");
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isServiceRunning = false;
        handler.removeCallbacks(runnable);
        handler.getLooper().quit();
        handlerThread.interrupt();
        handlerThread.stop();
        handlerThread.quit();
        Toast.makeText(this, "Zenohd service done", Toast.LENGTH_SHORT).show();
    }

    public boolean isRunning() {
        return isServiceRunning;
    }

    public void stopService() {
        this.isServiceRunning = false;
        handler.removeCallbacks(runnable);
        handlerThread.quit();
        stopSelf();
        Toast.makeText(this, "Zenohd service done", Toast.LENGTH_SHORT).show();
    }
}
