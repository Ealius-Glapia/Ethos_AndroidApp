package com.cardmaster.app.work;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.MainActivity;
import com.cardmaster.app.R;
import com.cardmaster.app.data.database.AppDatabase;
import com.cardmaster.app.data.dao.BoosterChargeDao;
import com.cardmaster.app.data.entity.BoosterCharge;

public class BoosterChargeReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "booster_charge_channel";
    private static final int NOTIFICATION_ID = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Initialize database
        AppDatabase database = AppDatabase.getInstance(context);
        BoosterChargeDao boosterChargeDao = database.boosterChargeDao();

        BoosterCharge boosterCharge = boosterChargeDao.getBoosterChargeSync();
        if (boosterCharge == null) {
            return;
        }

        if (boosterCharge.getCurrentCharge() < boosterCharge.getMaxCharge()) {
            long currentTime = System.currentTimeMillis();
            long lastChargeTime = boosterCharge.getLastChargeTime();
            long chargeDuration = boosterCharge.getChargeDurationMs();

            long elapsedTime = currentTime - lastChargeTime;
            int boostersToRecharge = (int) (elapsedTime / chargeDuration);

            if (boostersToRecharge > 0) {
                int newCharge = Math.min(boosterCharge.getCurrentCharge() + boostersToRecharge, boosterCharge.getMaxCharge());
                long newLastChargeTime = lastChargeTime + (boostersToRecharge * chargeDuration);

                boosterChargeDao.updateCharge(newCharge, newLastChargeTime);

                // If we reached max charge, show notification
                if (newCharge == boosterCharge.getMaxCharge()) {
                    // Create notification channel if needed (Android 8+)
                    createNotificationChannel(context);

                    // Create intent to open app when notification is clicked
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE
                    );

                    // Build notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_timer)
                            .setContentTitle(context.getString(R.string.notification_booster_recharged_title))
                            .setContentText(context.getString(R.string.notification_booster_recharged_text, newCharge, boosterCharge.getMaxCharge()))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    // Show notification
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booster Recharge",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications when boosters are recharged");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
