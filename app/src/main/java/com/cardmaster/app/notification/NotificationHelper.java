package com.cardmaster.app.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.cardmaster.app.MainActivity;
import com.cardmaster.app.R;

public class NotificationHelper {
    private static final String CHANNEL_ID_BOOSTER = "booster_recharge_channel";
    private static final String CHANNEL_ID_DAILY = "daily_reminder_channel";
    private static final int NOTIFICATION_ID_BOOSTER = 1001;
    private static final int NOTIFICATION_ID_DAILY = 1002;

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            // Booster recharge channel
            NotificationChannel boosterChannel = new NotificationChannel(
                    CHANNEL_ID_BOOSTER,
                    "Booster Recharge",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            boosterChannel.setDescription("Notifications when boosters are recharged");
            manager.createNotificationChannel(boosterChannel);
            
            // Daily reminder channel
            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_ID_DAILY,
                    "Daily Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyChannel.setDescription("Daily reminder to check the app");
            manager.createNotificationChannel(dailyChannel);
        }
    }

    public static void showBoosterRechargedNotification(Context context, int currentCharge, int maxCharge) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BOOSTER)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(context.getString(R.string.notification_booster_recharged_title))
                .setContentText(context.getString(R.string.notification_booster_recharged_text, currentCharge, maxCharge))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID_BOOSTER, builder.build());
    }

    public static void showDailyReminderNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(context.getString(R.string.notification_daily_reminder_title))
                .setContentText(context.getString(R.string.notification_daily_reminder_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID_DAILY, builder.build());
    }
}
