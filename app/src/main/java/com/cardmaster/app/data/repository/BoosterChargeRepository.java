package com.cardmaster.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.data.dao.BoosterChargeDao;
import com.cardmaster.app.data.entity.BoosterCharge;
import com.cardmaster.app.notification.NotificationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoosterChargeRepository {
    private final BoosterChargeDao boosterChargeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BoosterChargeRepository(BoosterChargeDao boosterChargeDao) {
        this.boosterChargeDao = boosterChargeDao;
    }

    public LiveData<BoosterCharge> getBoosterCharge() {
        return boosterChargeDao.getBoosterCharge();
    }

    public BoosterCharge getBoosterChargeSync() {
        return boosterChargeDao.getBoosterChargeSync();
    }

    public void initializeCharge() {
        executor.execute(() -> {
            BoosterCharge existing = boosterChargeDao.getBoosterChargeSync();
            if (existing == null) {
                BoosterCharge newCharge = new BoosterCharge();
                boosterChargeDao.insert(newCharge);
            }
        });
    }

    public void useBooster() {
        executor.execute(() -> {
            BoosterCharge charge = boosterChargeDao.getBoosterChargeSync();
            if (charge != null && charge.getCurrentCharge() > 0) {
                boolean wasAtMaxCharge = charge.getCurrentCharge() == charge.getMaxCharge();
                charge.setCurrentCharge(charge.getCurrentCharge() - 1);
                // If we were at max charge, start the timer from now
                if (wasAtMaxCharge) {
                    charge.setLastChargeTime(System.currentTimeMillis());
                }
                boosterChargeDao.update(charge);
            }
        });
    }

    public void incrementCharge() {
        executor.execute(() -> {
            BoosterCharge charge = boosterChargeDao.getBoosterChargeSync();
            if (charge != null && charge.getCurrentCharge() < charge.getMaxCharge()) {
                charge.setCurrentCharge(charge.getCurrentCharge() + 1);
                charge.setLastChargeTime(System.currentTimeMillis());
                boosterChargeDao.update(charge);
            }
        });
    }

    public void updateCharge(int charge, long lastChargeTime) {
        executor.execute(() -> {
            BoosterCharge oldCharge = boosterChargeDao.getBoosterChargeSync();
            boosterChargeDao.updateCharge(charge, lastChargeTime);
            
            // Show notification if charge increased
            if (oldCharge != null && charge > oldCharge.getCurrentCharge()) {
                Context context = CardMasterApplication.getInstance();
                NotificationHelper.showBoosterRechargedNotification(context, charge, oldCharge.getMaxCharge());
            }
        });
    }

    public void addBoosters(int amount) {
        executor.execute(() -> {
            BoosterCharge charge = boosterChargeDao.getBoosterChargeSync();
            if (charge != null) {
                charge.setCurrentCharge(charge.getCurrentCharge() + amount);
                boosterChargeDao.update(charge);
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
