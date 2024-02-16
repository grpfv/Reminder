package com.example.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.notif)
                .setContentTitle("Reminder: " + title)
                .setContentText("It's time for your reminder!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Set sound and vibration
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {0, 1000, 1000}; // Vibrate pattern: vibrate for 1 second, pause for 1 second, vibrate for 1 second
        builder.setSound(soundUri);
        builder.setVibrate(pattern);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Show notification
            notificationManager.notify(0, builder.build());
        }
    }
}
