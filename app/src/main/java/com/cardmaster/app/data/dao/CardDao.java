package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.entity.Booster;

import java.util.List;

@Dao
public interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Card> cards);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Card card);

    @Query("SELECT * FROM cards WHERE boosterId = :boosterId ORDER BY number ASC")
    LiveData<List<Card>> getCardsByBoosterId(int boosterId);

    @Query("SELECT * FROM cards WHERE boosterId = :boosterId ORDER BY number ASC")
    List<Card> getCardsByBoosterIdSync(int boosterId);

    @Query("SELECT * FROM cards WHERE id = :cardId")
    LiveData<Card> getCardById(int cardId);

    @Query("SELECT * FROM cards ORDER BY boosterId, number ASC")
    LiveData<List<Card>> getAllCards();

    @Transaction
    @Query("SELECT * FROM boosters")
    LiveData<List<BoosterWithCards>> getBoosterCardsMap();

    @Query("DELETE FROM cards")
    void deleteAll();

    @Query("UPDATE cards SET boosterId = :newBoosterId WHERE id = :cardId")
    void updateCardBoosterId(int cardId, int newBoosterId);
}
