package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cardmaster.app.data.entity.BoosterCharge;

@Dao
public interface BoosterChargeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BoosterCharge boosterCharge);

    @Update
    void update(BoosterCharge boosterCharge);

    @Query("SELECT * FROM booster_charge WHERE id = 1")
    LiveData<BoosterCharge> getBoosterCharge();

    @Query("SELECT * FROM booster_charge WHERE id = 1")
    BoosterCharge getBoosterChargeSync();

    @Query("UPDATE booster_charge SET currentCharge = currentCharge - 1 WHERE id = 1")
    void useBooster();

    @Query("UPDATE booster_charge SET currentCharge = :charge, lastChargeTime = :lastChargeTime WHERE id = 1")
    void updateCharge(int charge, long lastChargeTime);
}
