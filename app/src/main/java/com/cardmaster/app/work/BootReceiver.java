package com.cardmaster.app.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device booted, rescheduling alarms");
            
            // Reschedule daily reminder alarm
            AlarmScheduler.scheduleDailyReminder(context);
            
            // Note: Booster charge alarm will be scheduled when app goes to background
        }
    }
}
