package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cardmaster.app.data.entity.Booster;

import java.util.List;

@Dao
public interface BoosterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Booster> boosters);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Booster booster);

    @Query("SELECT * FROM boosters ORDER BY releaseDate DESC")
    LiveData<List<Booster>> getAllBoosters();

    @Query("SELECT * FROM boosters WHERE id = :boosterId")
    LiveData<Booster> getBoosterById(int boosterId);

    @Query("SELECT * FROM boosters WHERE id = :boosterId")
    Booster getBoosterByIdSync(int boosterId);

    @Query("DELETE FROM boosters")
    void deleteAll();

    @Query("UPDATE boosters SET status = :status WHERE id = :boosterId")
    void updateStatus(int boosterId, String status);

    @Query("SELECT * FROM boosters ORDER BY releaseDate DESC")
    List<Booster> getAllBoostersSync();

    @Query("SELECT * FROM boosters WHERE status != 'active' ORDER BY releaseDate DESC")
    List<Booster> getInactiveBoostersSync();
}
