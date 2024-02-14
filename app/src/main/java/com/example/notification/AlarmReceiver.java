package com.example.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notif)
                .setContentTitle("Reminder: " + title)
                .setContentText("It's time for your reminder!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create notification channel for API level 26 and above
                String channelId = "default_channel_id";
                CharSequence channelName = "Default Channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            // Show notification
            notificationManager.notify(0, builder.build());
        }
    }
}
