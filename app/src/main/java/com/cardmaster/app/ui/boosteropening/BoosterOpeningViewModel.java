package com.cardmaster.app.ui.boosteropening;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoosterOpeningViewModel extends ViewModel {
    private final CardRepository cardRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final CardSelectionHelper cardSelectionHelper;
    private int boosterId;
    private final MutableLiveData<List<Card>> revealedCards = new MutableLiveData<>();
    private final MutableLiveData<Boolean> openingComplete = new MutableLiveData<>();
    int tapCount = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BoosterOpeningViewModel(CardRepository cardRepository, OwnedCardRepository ownedCardRepository) {
        this.cardRepository = cardRepository;
        this.ownedCardRepository = ownedCardRepository;
        this.cardSelectionHelper = new CardSelectionHelper();
   }

    public void setBoosterId(int boosterId) {
        this.boosterId = boosterId;
    }

    public int getBoosterId() {
        return boosterId;
    }

    public LiveData<List<Card>> getRevealedCards() {
        return revealedCards;
    }

    public LiveData<Boolean> getOpeningComplete() {
        return openingComplete;
    }

    public void onPackTap() {
        tapCount++;
        if (tapCount >= 5) {
            openBooster();
        }
    }

    public void onSwipeGesture() {
        openBooster();
    }

    private void openBooster() {
        executor.execute(() -> {
            android.util.Log.d("BoosterOpening", "Opening booster with ID: " + boosterId);
            List<Card> allCards = cardRepository.getCardsByBoosterIdSync(boosterId);
            android.util.Log.d("BoosterOpening", "Found " + (allCards != null ? allCards.size() : 0) + " cards in database");
            
            if (allCards == null || allCards.isEmpty()) {
                android.util.Log.e("BoosterOpening", "No cards found for booster ID: " + boosterId);
                return;
            }
            
            List<Card> selectedCards = generateRandomCards(allCards, 5);
            android.util.Log.d("BoosterOpening", "Selected " + selectedCards.size() + " cards");
            revealedCards.postValue(selectedCards);
            openingComplete.postValue(true);
        });
    }

    private List<Card> generateRandomCards(List<Card> availableCards, int count) {
        List<Card> cards = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            CardSelectionHelper.CardSelectionResult result = cardSelectionHelper.selectCard();
            Card selectedCard = findCardByUpgradeAndRarity(availableCards, result.upgrade, result.rarity);
            if (selectedCard != null) {
                cards.add(selectedCard);
            } else {
                // Find closest rarity/upgrade if no match found
                selectedCard = findClosestCard(availableCards, result.upgrade, result.rarity);
                if (selectedCard != null) {
                    cards.add(selectedCard);
                }
            }
        }
        
        // Sort cards by rarity + upgrade sum (ascending)
        cards.sort((card1, card2) -> {
            int sum1 = Integer.parseInt(card1.getRarity()) + card1.getNumber();
            int sum2 = Integer.parseInt(card2.getRarity()) + card2.getNumber();
            return Integer.compare(sum1, sum2);
        });
        
        return cards;
    }

    private Card findCardByUpgradeAndRarity(List<Card> cards, int upgrade, int rarity) {
        List<Card> matchingCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.getNumber() == upgrade && Integer.parseInt(card.getRarity()) == rarity) {
                matchingCards.add(card);
            }
        }
        
        if (matchingCards.isEmpty()) {
            return null;
        }
        
        // Calculate weighted selection based on how many times each card has been obtained
        // Weight decreases linearly with quantity, but never reaches 0
        // At 20 times, weight is about 40% of original (reduction factor of 0.03)
        // Minimum weight is 20% to ensure non-zero probability
        final double REDUCTION_FACTOR = 0.03;
        final double MIN_WEIGHT = 0.2;
        
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0.0;
        
        for (Card card : matchingCards) {
            int quantity = ownedCardRepository.getOwnedCardDao().getQuantityByCardIdSync(card.getId());
            double weight = 1.0 - (quantity * REDUCTION_FACTOR);
            weight = Math.max(weight, MIN_WEIGHT);
            weights.add(weight);
            totalWeight += weight;
        }
        
        // Select card based on weights
        Random random = new Random();
        double randomValue = random.nextDouble() * totalWeight;
        
        double cumulativeWeight = 0.0;
        for (int i = 0; i < matchingCards.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue <= cumulativeWeight) {
                return matchingCards.get(i);
            }
        }
        
        // Fallback to last card if something goes wrong
        return matchingCards.get(matchingCards.size() - 1);
    }

    private Card findClosestCard(List<Card> cards, int targetUpgrade, int targetRarity) {
        if (cards.isEmpty()) {
            return null;
        }

        Card closestCard = null;
        double minDistance = Double.MAX_VALUE;

        for (Card card : cards) {
            int cardUpgrade = card.getNumber();
            int cardRarity = Integer.parseInt(card.getRarity());
            
            // Calculate distance (weighted: rarity is more important than upgrade)
            double upgradeDistance = Math.abs(cardUpgrade - targetUpgrade) * 0.3;
            double rarityDistance = Math.abs(cardRarity - targetRarity) * 1.0;
            double totalDistance = upgradeDistance + rarityDistance;
            
            if (totalDistance < minDistance) {
                minDistance = totalDistance;
                closestCard = card;
            }
        }

        return closestCard;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
