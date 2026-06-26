package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "booster_charge")
public class BoosterCharge {
    @PrimaryKey
    private int id;
    private int currentCharge;
    private int maxCharge;
    private long lastChargeTime;
    private long chargeDurationMs; // Time in milliseconds to recharge one booster (e.g., 10 minutes = 600000)

    public BoosterCharge() {
        this.id = 1; // Single row for booster charge
        this.currentCharge = 5;
        this.maxCharge = 5;
        this.lastChargeTime = System.currentTimeMillis();
        this.chargeDurationMs = 600000; // 10 minutes
    }

    public BoosterCharge(int currentCharge, int maxCharge, long lastChargeTime, long chargeDurationMs) {
        this.id = 1;
        this.currentCharge = currentCharge;
        this.maxCharge = maxCharge;
        this.lastChargeTime = lastChargeTime;
        this.chargeDurationMs = chargeDurationMs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(int currentCharge) {
        this.currentCharge = currentCharge;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public void setMaxCharge(int maxCharge) {
        this.maxCharge = maxCharge;
    }

    public long getLastChargeTime() {
        return lastChargeTime;
    }

    public void setLastChargeTime(long lastChargeTime) {
        this.lastChargeTime = lastChargeTime;
    }

    public long getChargeDurationMs() {
        return chargeDurationMs;
    }

    public void setChargeDurationMs(long chargeDurationMs) {
        this.chargeDurationMs = chargeDurationMs;
    }
}
