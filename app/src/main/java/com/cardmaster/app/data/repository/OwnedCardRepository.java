package com.cardmaster.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import java.util.HashMap;

import com.cardmaster.app.data.dao.OwnedCardDao;
import com.cardmaster.app.data.entity.OwnedCard;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OwnedCardRepository {
    private final OwnedCardDao ownedCardDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public OwnedCardRepository(OwnedCardDao ownedCardDao) {
        this.ownedCardDao = ownedCardDao;
    }

    public LiveData<List<OwnedCard>> getAllOwnedCards() {
        return ownedCardDao.getAllOwnedCards();
    }

    public LiveData<List<Card>> getOwnedCardsWithDetails() {
        return ownedCardDao.getOwnedCardsWithDetails();
    }

    public LiveData<Map<Integer, Card>> getOwnedCardsMap() {
        return Transformations.map(ownedCardDao.getOwnedCardsList(), cards -> {
            Map<Integer, Card> map = new HashMap<>();

            if (cards != null) {
                for (Card card : cards) {
                    map.put(card.getId(), card);
                }
            }

            return map;
        });
    }



    public LiveData<OwnedCard> getOwnedCardByCardId(int cardId) {
        return ownedCardDao.getOwnedCardByCardId(cardId);
    }

    public LiveData<Integer> getQuantityByCardId(int cardId) {
        return ownedCardDao.getQuantityByCardId(cardId);
    }

    public LiveData<Integer> getTotalUniqueCards() {
        return ownedCardDao.getTotalUniqueCards();
    }

    public LiveData<Integer> getLoadedUniqueCards() {
        return ownedCardDao.getLoadedUniqueCards();
    }

    public void insertOwnedCard(OwnedCard ownedCard) {
        executor.execute(() -> {
            android.util.Log.d("OwnedCardRepository", "Inserting owned card with cardId: " + ownedCard.getCardId());
            int currentQuantity = ownedCardDao.getQuantityByCardIdSync(ownedCard.getCardId());
            android.util.Log.d("OwnedCardRepository", "Current quantity: " + currentQuantity);
            if (currentQuantity > 0) {
                android.util.Log.d("OwnedCardRepository", "Incrementing quantity for cardId: " + ownedCard.getCardId());
                ownedCardDao.incrementQuantity(ownedCard.getCardId());
            } else {
                android.util.Log.d("OwnedCardRepository", "Inserting new owned card with cardId: " + ownedCard.getCardId());
                ownedCardDao.insert(ownedCard);
            }
        });
    }

    public void insertOwnedCards(List<OwnedCard> ownedCards) {
        executor.execute(() -> ownedCardDao.insertAll(ownedCards));
    }

    public void shutdown() {
        executor.shutdown();
    }

    public OwnedCardDao getOwnedCardDao() {
        return ownedCardDao;
    }
}
