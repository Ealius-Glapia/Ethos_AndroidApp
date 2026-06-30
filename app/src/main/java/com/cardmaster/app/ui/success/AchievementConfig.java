package com.cardmaster.app.ui.success;

import java.util.List;
import java.util.Map;

/**
 * Configuration class for achievements to make them easily parameterizable.
 * Supports translations via string resources and custom card probabilities for booster rewards.
 */
public class AchievementConfig {
    private int id;
    private int titleResId;          // String resource ID for title (e.g., R.string.achievement_title)
    private int descriptionResId;    // String resource ID for description
    private String conditionType;    // "cards_unlocked", "all_levels_1_9", or custom
    private int conditionValue;      // Value for condition (e.g., 100 for cards_unlocked)
    private String rewardType;       // "tokens" or "booster_custom"
    private int rewardValue;         // Token amount or number of cards for booster
    private boolean isClaimed;
    
    // For booster rewards: list of probability distributions
    // Each entry represents a card slot with its probability distribution
    // Format: List<Map<Integer, Double>> where each map is {cardId: probability}
    private List<Map<Integer, Double>> cardProbabilities;

    public AchievementConfig(int id, int titleResId, int descriptionResId, 
                           String conditionType, int conditionValue, 
                           String rewardType, int rewardValue, 
                           boolean isClaimed) {
        this.id = id;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.isClaimed = isClaimed;
    }

    public AchievementConfig(int id, int titleResId, int descriptionResId, 
                           String conditionType, int conditionValue, 
                           String rewardType, int rewardValue, 
                           boolean isClaimed, List<Map<Integer, Double>> cardProbabilities) {
        this.id = id;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.isClaimed = isClaimed;
        this.cardProbabilities = cardProbabilities;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTitleResId() { return titleResId; }
    public void setTitleResId(int titleResId) { this.titleResId = titleResId; }
    
    public int getDescriptionResId() { return descriptionResId; }
    public void setDescriptionResId(int descriptionResId) { this.descriptionResId = descriptionResId; }
    
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    
    public int getConditionValue() { return conditionValue; }
    public void setConditionValue(int conditionValue) { this.conditionValue = conditionValue; }
    
    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }
    
    public int getRewardValue() { return rewardValue; }
    public void setRewardValue(int rewardValue) { this.rewardValue = rewardValue; }
    
    public boolean isClaimed() { return isClaimed; }
    public void setClaimed(boolean claimed) { isClaimed = claimed; }
    
    public List<Map<Integer, Double>> getCardProbabilities() { return cardProbabilities; }
    public void setCardProbabilities(List<Map<Integer, Double>> cardProbabilities) { 
        this.cardProbabilities = cardProbabilities; 
    }
}
