package com.cardmaster.app.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cardmaster.app.notification.NotificationHelper;

public class DailyReminderWorker extends Worker {
    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Show daily reminder notification
        NotificationHelper.showDailyReminderNotification(getApplicationContext());
        return Result.success();
    }
}
