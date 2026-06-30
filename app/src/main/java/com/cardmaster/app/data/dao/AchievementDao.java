package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cardmaster.app.data.entity.Achievement;

import java.util.List;

@Dao
public interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Achievement achievement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Achievement> achievements);

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    LiveData<List<Achievement>> getAllAchievements();

    @Query("SELECT * FROM achievements WHERE id = :id")
    LiveData<Achievement> getAchievementById(int id);

    @Query("SELECT * FROM achievements WHERE id = :id")
    Achievement getAchievementByIdSync(int id);

    @Query("SELECT * FROM achievements")
    List<Achievement> getAllAchievementsSync();

    @Update
    void update(Achievement achievement);

    @Query("UPDATE achievements SET isClaimed = 1 WHERE id = :id")
    void markAsClaimed(int id);

    @Query("DELETE FROM achievements")
    void deleteAll();
}
