package com.cardmaster.app.ui.collection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cardmaster.app.data.dao.BoosterWithCards;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.repository.BoosterRepository;
import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionViewModel extends ViewModel {
    private final BoosterRepository boosterRepository;
    private final CardRepository cardRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final LiveData<List<BoosterWithCards>> filteredBoosterCardsMap;

    public CollectionViewModel(BoosterRepository boosterRepository, CardRepository cardRepository,
                               OwnedCardRepository ownedCardRepository) {
        this.boosterRepository = boosterRepository;
        this.cardRepository = cardRepository;
        this.ownedCardRepository = ownedCardRepository;
        
        // Filter out hard deleted boosters from collection
        this.filteredBoosterCardsMap = Transformations.map(cardRepository.getBoosterCardsMap(), boosterWithCardsList -> {
            List<BoosterWithCards> filtered = new ArrayList<>();
            if (boosterWithCardsList != null) {
                for (BoosterWithCards boosterWithCards : boosterWithCardsList) {
                    Booster booster = boosterWithCards.booster;
                    // Show in collection if active or soft deleted, hide if hard deleted
                    if (booster != null && !"hard_deleted".equals(booster.getStatus())) {
                        filtered.add(boosterWithCards);
                    }
                }
            }
            return filtered;
        });
    }

    public LiveData<List<BoosterWithCards>> getBoosterCardsMap() {
        return filteredBoosterCardsMap;
    }

    public LiveData<Map<Integer, Card>> getOwnedCardsMap() {
        return ownedCardRepository.getOwnedCardsMap();
    }

    public LiveData<Integer> getTotalUniqueCards() {
        return ownedCardRepository.getTotalUniqueCards();
    }

    public LiveData<Integer> getLoadedUniqueCards() {
        return ownedCardRepository.getLoadedUniqueCards();
    }
}
