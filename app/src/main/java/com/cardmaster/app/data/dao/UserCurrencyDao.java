package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cardmaster.app.data.entity.UserCurrency;

@Dao
public interface UserCurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserCurrency userCurrency);

    @Update
    void update(UserCurrency userCurrency);

    @Query("SELECT * FROM user_currency WHERE id = 1")
    LiveData<UserCurrency> getUserCurrency();

    @Query("SELECT * FROM user_currency WHERE id = 1")
    UserCurrency getUserCurrencySync();

    @Query("UPDATE user_currency SET tokens = tokens + :amount WHERE id = 1")
    void addTokens(int amount);

    @Query("UPDATE user_currency SET premiumTokens = premiumTokens + :amount WHERE id = 1")
    void addPremiumTokens(int amount);

    @Query("UPDATE user_currency SET tokens = tokens - :amount WHERE id = 1")
    void subtractTokens(int amount);

    @Query("UPDATE user_currency SET premiumTokens = premiumTokens - :amount WHERE id = 1")
    void subtractPremiumTokens(int amount);
}
