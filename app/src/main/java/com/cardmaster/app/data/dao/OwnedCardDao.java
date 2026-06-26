package com.cardmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapColumn;
import androidx.room.MapInfo;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.cardmaster.app.data.entity.OwnedCard;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.Map;

@Dao
public interface OwnedCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OwnedCard ownedCard);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OwnedCard> ownedCards);

    @Query("SELECT * FROM owned_cards WHERE cardId = :cardId")
    LiveData<OwnedCard> getOwnedCardByCardId(int cardId);

    @Query("SELECT * FROM owned_cards ORDER BY obtainedAt DESC")
    LiveData<List<OwnedCard>> getAllOwnedCards();

    @Query("SELECT * FROM cards WHERE id IN (SELECT DISTINCT cardId FROM owned_cards) ORDER BY id ASC")
    LiveData<List<Card>> getOwnedCardsWithDetails();

    @Query("SELECT * FROM cards WHERE id IN (SELECT DISTINCT cardId FROM owned_cards)")
    LiveData<List<Card>> getOwnedCardsList();

    @Query("SELECT COUNT(*) FROM owned_cards WHERE cardId = :cardId")
    LiveData<Integer> getQuantityByCardId(int cardId);

    @Query("SELECT COUNT(*) FROM owned_cards WHERE cardId = :cardId")
    int getQuantityByCardIdSync(int cardId);

    @Query("SELECT COUNT(DISTINCT cardId) FROM owned_cards")
    LiveData<Integer> getTotalUniqueCards();

    @Query("SELECT COUNT(DISTINCT cardId) FROM owned_cards WHERE cardId IN (SELECT id FROM cards WHERE boosterId IN (SELECT id FROM boosters WHERE status != 'hard_deleted'))")
    LiveData<Integer> getLoadedUniqueCards();

    @Query("DELETE FROM owned_cards")
    void deleteAll();

    @Transaction
    @Query("UPDATE owned_cards SET quantity = quantity + 1 WHERE cardId = :cardId")
    void incrementQuantity(int cardId);

    @Query("SELECT COUNT(*) FROM owned_cards WHERE cardId = :cardId")
    boolean isCardOwnedSync(int cardId);
}
