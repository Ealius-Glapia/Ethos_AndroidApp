package com.cardmaster.app.data.repository;

import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.entity.Booster;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoosterRepository {
    private final BoosterDao boosterDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BoosterRepository(BoosterDao boosterDao) {
        this.boosterDao = boosterDao;
    }

    public LiveData<List<Booster>> getAllBoosters() {
        return boosterDao.getAllBoosters();
    }

    public LiveData<Booster> getBoosterById(int boosterId) {
        return boosterDao.getBoosterById(boosterId);
    }

    public Booster getBoosterByIdSync(int boosterId) {
        return boosterDao.getBoosterByIdSync(boosterId);
    }

    public List<Booster> getAllBoostersSync() {
        return boosterDao.getAllBoostersSync();
    }

    public void insertBooster(Booster booster) {
        executor.execute(() -> boosterDao.insert(booster));
    }

    public void insertBoosterSync(Booster booster) {
        boosterDao.insert(booster);
    }

    public void insertBoosters(List<Booster> boosters) {
        executor.execute(() -> boosterDao.insertAll(boosters));
    }

    public void updateStatus(int boosterId, String status) {
        executor.execute(() -> boosterDao.updateStatus(boosterId, status));
    }

    public void updateOrderIndex(int boosterId, int orderIndex) {
        executor.execute(() -> boosterDao.updateOrderIndex(boosterId, orderIndex));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
