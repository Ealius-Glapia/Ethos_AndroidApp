package com.cardmaster.app.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.data.entity.BoosterCharge;
import com.cardmaster.app.data.repository.BoosterChargeRepository;
import com.cardmaster.app.notification.NotificationHelper;

public class BoosterChargeWorker extends Worker {
    public BoosterChargeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        BoosterChargeRepository repository = app.getBoosterChargeRepository();
        BoosterCharge boosterCharge = repository.getBoosterChargeSync();
        
        if (boosterCharge != null && boosterCharge.getCurrentCharge() < boosterCharge.getMaxCharge()) {
            long currentTime = System.currentTimeMillis();
            long lastChargeTime = boosterCharge.getLastChargeTime();
            long chargeDuration = boosterCharge.getChargeDurationMs();
            
            long elapsedTime = currentTime - lastChargeTime;
            int boostersToRecharge = (int) (elapsedTime / chargeDuration);
            
            if (boostersToRecharge > 0) {
                int newCharge = Math.min(boosterCharge.getCurrentCharge() + boostersToRecharge, boosterCharge.getMaxCharge());
                long newLastChargeTime = lastChargeTime + (boostersToRecharge * chargeDuration);
                
                repository.updateCharge(newCharge, newLastChargeTime);
                
                // If we reached max charge, show notification
                if (newCharge == boosterCharge.getMaxCharge()) {
                    NotificationHelper.showBoosterRechargedNotification(
                            getApplicationContext(),
                            newCharge,
                            boosterCharge.getMaxCharge()
                    );
                }
            }
        }
        
        return Result.success();
    }
}
