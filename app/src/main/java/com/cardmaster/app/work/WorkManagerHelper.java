package com.cardmaster.app.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {
    private static final String DAILY_REMINDER_WORK_NAME = "daily_reminder_work";
    private static final String BOOSTER_CHARGE_WORK_NAME = "booster_charge_work";

    public static void scheduleDailyReminder(Context context) {
        PeriodicWorkRequest dailyReminderRequest = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class,
                24,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DAILY_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyReminderRequest
        );
    }

    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK_NAME);
    }

    public static void scheduleBoosterChargeCheck(Context context) {
        // Schedule periodic work every 15 minutes to check and update booster charge
        PeriodicWorkRequest chargeRequest = new PeriodicWorkRequest.Builder(
                BoosterChargeWorker.class,
                15,
                TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BOOSTER_CHARGE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                chargeRequest
        );
    }

    public static void cancelBoosterChargeCheck(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(BOOSTER_CHARGE_WORK_NAME);
    }
}
