package com.cardmaster.app.data.repository;

import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.dao.CardDao;
import com.cardmaster.app.data.dao.BoosterWithCards;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardRepository {
    private final CardDao cardDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CardRepository(CardDao cardDao) {
        this.cardDao = cardDao;
    }

    public LiveData<List<Card>> getAllCards() {
        return cardDao.getAllCards();
    }

    public LiveData<List<Card>> getCardsByBoosterId(int boosterId) {
        return cardDao.getCardsByBoosterId(boosterId);
    }

    public List<Card> getCardsByBoosterIdSync(int boosterId) {
        return cardDao.getCardsByBoosterIdSync(boosterId);
    }

    public LiveData<Card> getCardById(int cardId) {
        return cardDao.getCardById(cardId);
    }

    public LiveData<List<BoosterWithCards>> getBoosterCardsMap() {
        return cardDao.getBoosterCardsMap();
    }

    public void insertCard(Card card) {
        executor.execute(() -> cardDao.insert(card));
    }

    public void insertCards(List<Card> cards) {
        executor.execute(() -> cardDao.insertAll(cards));
    }

    public void updateCardBoosterId(int cardId, int newBoosterId) {
        executor.execute(() -> cardDao.updateCardBoosterId(cardId, newBoosterId));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
