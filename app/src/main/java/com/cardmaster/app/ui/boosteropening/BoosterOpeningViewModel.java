package com.cardmaster.app.ui.boosteropening;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.repository.CardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoosterOpeningViewModel extends ViewModel {
    private final CardRepository cardRepository;
    private final CardSelectionHelper cardSelectionHelper;
    private int boosterId;
    private final MutableLiveData<List<Card>> revealedCards = new MutableLiveData<>();
    private final MutableLiveData<Boolean> openingComplete = new MutableLiveData<>();
    int tapCount = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BoosterOpeningViewModel(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
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
        
        // Return random card from matching cards
        Random random = new Random();
        return matchingCards.get(random.nextInt(matchingCards.size()));
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
