package com.example.zenoh;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ZenohdService extends Service {
    private static final String TAG = ZenohdService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "zenohd";
    private final IBinder binder = new ZenohdBinder();
    private HandlerThread handlerThread;
    private Handler handler;
    private boolean isServiceRunning = false;
    private Runnable runnable;
    public class ZenohdBinder extends Binder {
        ZenohdService getService() {
            return ZenohdService.this;
        }
    }

    @Override
    public void onCreate() {
        handlerThread = new HandlerThread("ZenohdServiceThread",
                Process.THREAD_PRIORITY_BACKGROUND);
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
        runnable = () -> {
            try {
                int i = 0;
                while (handlerThread.isAlive()) {
                    Thread.sleep(1000);
                    Log.d(TAG, "Service running: " + i);
                    i++;
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Service error.", e);
            }
        };
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
