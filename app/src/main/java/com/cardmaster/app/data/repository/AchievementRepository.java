package com.cardmaster.app.data.repository;

import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.dao.AchievementDao;
import com.cardmaster.app.data.entity.Achievement;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AchievementRepository {
    private final AchievementDao achievementDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AchievementRepository(AchievementDao achievementDao) {
        this.achievementDao = achievementDao;
    }

    public LiveData<List<Achievement>> getAllAchievements() {
        return achievementDao.getAllAchievements();
    }

    public LiveData<Achievement> getAchievementById(int id) {
        return achievementDao.getAchievementById(id);
    }

    public Achievement getAchievementByIdSync(int id) {
        return achievementDao.getAchievementByIdSync(id);
    }

    public List<Achievement> getAllAchievementsSync() {
        return achievementDao.getAllAchievementsSync();
    }

    public void insert(Achievement achievement) {
        executor.execute(() -> achievementDao.insert(achievement));
    }

    public void insertAll(List<Achievement> achievements) {
        executor.execute(() -> achievementDao.insertAll(achievements));
    }

    public void update(Achievement achievement) {
        executor.execute(() -> achievementDao.update(achievement));
    }

    public void markAsClaimed(int id) {
        executor.execute(() -> achievementDao.markAsClaimed(id));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
