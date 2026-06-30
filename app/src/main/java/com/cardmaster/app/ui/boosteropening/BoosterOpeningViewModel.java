package com.cardmaster.app.ui.boosteropening;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.data.entity.Achievement;
import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoosterOpeningViewModel extends ViewModel {
    private final CardRepository cardRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final CardSelectionHelper cardSelectionHelper;
    private int boosterId;
    private int achievementId;
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

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
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
            android.util.Log.d("BoosterOpening", "Opening booster with ID: " + boosterId + ", achievementId: " + achievementId);
            List<Card> allCards = cardRepository.getCardsByBoosterIdSync(boosterId);
            android.util.Log.d("BoosterOpening", "Found " + (allCards != null ? allCards.size() : 0) + " cards in database");
            
            if (allCards == null || allCards.isEmpty()) {
                android.util.Log.e("BoosterOpening", "No cards found for booster ID: " + boosterId);
                return;
            }
            
            List<Card> selectedCards;
            if (achievementId != -1) {
                // Get achievement configuration
                CardMasterApplication app = CardMasterApplication.getInstance();
                Achievement achievement = app.getAchievementRepository().getAchievementByIdSync(achievementId);
                String cardProbabilitiesJson = achievement != null ? achievement.getCardProbabilitiesJson() : null;
                
                // Use rigged probabilities from JSON for achievement reward
                selectedCards = generateCardsFromJson(allCards, cardProbabilitiesJson);
            } else {
                // Normal random selection
                selectedCards = generateRandomCards(allCards, 5);
            }
            
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

    private List<Card> generateRiggedCards(List<Card> availableCards) {
        List<Card> cards = new ArrayList<>();
        java.util.Set<Integer> selectedCardIds = new java.util.HashSet<>();
        
        // Generate one card of each level 1-9 with rare rarity (rarity 2)
        for (int level = 1; level <= 9; level++) {
            Card selectedCard = findCardByUpgradeAndRarity(availableCards, level, 2, selectedCardIds);
            if (selectedCard != null) {
                cards.add(selectedCard);
                selectedCardIds.add(selectedCard.getId());
            } else {
                // If no exact match, find closest card with rare rarity
                selectedCard = findClosestCardWithRarity(availableCards, level, 2, selectedCardIds);
                if (selectedCard != null) {
                    cards.add(selectedCard);
                    selectedCardIds.add(selectedCard.getId());
                }
            }
        }
        
        // Sort cards by level (ascending)
        cards.sort((card1, card2) -> Integer.compare(card1.getNumber(), card2.getNumber()));
        
        return cards;
    }

    private Card findClosestCardWithRarity(List<Card> cards, int targetUpgrade, int targetRarity, java.util.Set<Integer> excludedIds) {
        if (cards.isEmpty()) {
            return null;
        }

        Card closestCard = null;
        double minDistance = Double.MAX_VALUE;

        for (Card card : cards) {
            int cardUpgrade = card.getNumber();
            int cardRarity = Integer.parseInt(card.getRarity());
            
            // Only consider cards with the target rarity and not already selected
            if (cardRarity != targetRarity || excludedIds.contains(card.getId())) {
                continue;
            }
            
            double distance = Math.abs(cardUpgrade - targetUpgrade);
            
            if (distance < minDistance) {
                minDistance = distance;
                closestCard = card;
            }
        }

        return closestCard;
    }

    private Card findCardByUpgradeAndRarity(List<Card> cards, int upgrade, int rarity, java.util.Set<Integer> excludedIds) {
        List<Card> matchingCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.getNumber() == upgrade && Integer.parseInt(card.getRarity()) == rarity && !excludedIds.contains(card.getId())) {
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

    // Overload for normal card generation without exclusion
    private Card findCardByUpgradeAndRarity(List<Card> cards, int upgrade, int rarity) {
        return findCardByUpgradeAndRarity(cards, upgrade, rarity, new HashSet<>());
    }

    private List<Card> generateCardsFromJson(List<Card> availableCards, String cardProbabilitiesJson) {
        List<Card> cards = new ArrayList<>();
        
        if (cardProbabilitiesJson == null || cardProbabilitiesJson.isEmpty()) {
            // Fallback to rigged cards if no JSON provided
            return generateRiggedCards(availableCards);
        }
        
        try {
            JSONObject config = new JSONObject(cardProbabilitiesJson);
            String type = config.optString("type", "card_draws");
            
            if ("card_draws".equals(type)) {
                // Use separate probability sets for each card draw
                JSONArray cardDrawsArray = config.optJSONArray("cardDraws");
                if (cardDrawsArray != null) {
                    Set<Integer> selectedCardIds = new HashSet<>();
                    Random random = new Random();
                    
                    // Parse all card draws first
                    List<List<CardProbability>> allCardProbabilities = new ArrayList<>();
                    for (int i = 0; i < cardDrawsArray.length(); i++) {
                        JSONObject drawConfig = cardDrawsArray.getJSONObject(i);
                        JSONArray probabilitiesArray = drawConfig.optJSONArray("probabilities");
                        
                        if (probabilitiesArray != null) {
                            List<CardProbability> cardProbabilities = new ArrayList<>();
                            double totalProbability = 0.0;
                            
                            for (int j = 0; j < probabilitiesArray.length(); j++) {
                                JSONObject probConfig = probabilitiesArray.getJSONObject(j);
                                int upgrade = probConfig.getInt("upgrade");
                                int rarity = probConfig.getInt("rarity");
                                double probability = probConfig.getDouble("probability");
                                
                                if (probability > 0) {
                                    cardProbabilities.add(new CardProbability(upgrade, rarity, probability));
                                    totalProbability += probability;
                                }
                            }
                            
                            // Normalize probabilities if sum is not 1
                            if (totalProbability > 0 && Math.abs(totalProbability - 1.0) > 0.001) {
                                for (CardProbability cp : cardProbabilities) {
                                    cp.probability /= totalProbability;
                                }
                            }
                            
                            allCardProbabilities.add(cardProbabilities);
                        }
                    }
                    
                    // Process each card draw with fallback logic
                    for (int i = 0; i < allCardProbabilities.size(); i++) {
                        List<CardProbability> cardProbabilities = allCardProbabilities.get(i);
                        Card selectedCard = null;
                        
                        // Check if at least one probability in current draw is possible
                        boolean hasAnyPossible = false;
                        for (CardProbability cp : cardProbabilities) {
                            Card testCard = findCardByUpgradeAndRarity(availableCards, cp.upgrade, cp.rarity, selectedCardIds);
                            if (testCard != null) {
                                hasAnyPossible = true;
                                break;
                            }
                        }
                        
                        // If no probability is possible in current draw, fallback to previous draws
                        int drawIndex = i;
                        while (!hasAnyPossible && drawIndex > 0) {
                            drawIndex--;
                            cardProbabilities = allCardProbabilities.get(drawIndex);
                            for (CardProbability cp : cardProbabilities) {
                                Card testCard = findCardByUpgradeAndRarity(availableCards, cp.upgrade, cp.rarity, selectedCardIds);
                                if (testCard != null) {
                                    hasAnyPossible = true;
                                    break;
                                }
                            }
                        }
                        
                        // Select card from the chosen draw
                        if (hasAnyPossible) {
                            selectedCard = selectCardFromProbabilities(availableCards, cardProbabilities, selectedCardIds, random);
                        }
                        
                        // If still no card selected, try to find any available card
                        if (selectedCard == null) {
                            android.util.Log.w("BoosterOpening", "No card found for draw " + i + ", trying any available card");
                            for (Card card : availableCards) {
                                if (!selectedCardIds.contains(card.getId())) {
                                    selectedCard = card;
                                    android.util.Log.d("BoosterOpening", "Selected fallback card: " + card.getId() + " (upgrade=" + card.getNumber() + ", rarity=" + card.getRarity() + ")");
                                    break;
                                }
                            }
                        }
                        
                        // Add the selected card if found
                        if (selectedCard != null) {
                            cards.add(selectedCard);
                            selectedCardIds.add(selectedCard.getId());
                            android.util.Log.d("BoosterOpening", "Draw " + i + ": Selected card " + selectedCard.getId() + " (upgrade=" + selectedCard.getNumber() + ", rarity=" + selectedCard.getRarity() + ")");
                        } else {
                            android.util.Log.e("BoosterOpening", "Failed to select any card for draw " + i);
                        }
                    }
                }
            }
            
            // Sort cards by upgrade (ascending)
            cards.sort((card1, card2) -> Integer.compare(card1.getNumber(), card2.getNumber()));
            
        } catch (JSONException e) {
            android.util.Log.e("BoosterOpening", "Error parsing card probabilities JSON", e);
            // Fallback to rigged cards on error
            return generateRiggedCards(availableCards);
        }
        
        return cards;
    }

    // Helper class to hold card probability information
    private static class CardProbability {
        int upgrade;
        int rarity;
        double probability;
        
        CardProbability(int upgrade, int rarity, double probability) {
            this.upgrade = upgrade;
            this.rarity = rarity;
            this.probability = probability;
        }
    }

    private Card selectCardFromProbabilities(List<Card> availableCards, List<CardProbability> cardProbabilities, Set<Integer> selectedCardIds, Random random) {
        // Try each probability entry in order until we find a matching card
        for (CardProbability cp : cardProbabilities) {
            Card selectedCard = findCardByUpgradeAndRarity(availableCards, cp.upgrade, cp.rarity, selectedCardIds);
            if (selectedCard != null) {
                return selectedCard;
            }
        }
        
        // If no exact match found, try closest card for each probability
        for (CardProbability cp : cardProbabilities) {
            Card selectedCard = findClosestCardWithRarity(availableCards, cp.upgrade, cp.rarity, selectedCardIds);
            if (selectedCard != null) {
                return selectedCard;
            }
        }
        
        return null;
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
