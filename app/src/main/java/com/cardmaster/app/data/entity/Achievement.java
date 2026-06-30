package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "achievements")
public class Achievement {
    @PrimaryKey
    private int id;
    private int titleResId;          // String resource ID for title
    private int descriptionResId;    // String resource ID for description
    private String conditionType;    // "cards_unlocked", "all_levels_1_9", or custom
    private int conditionValue;      // Value for condition
    private String rewardType;       // "tokens", "booster_custom", "booster_rare"
    private int rewardValue;         // Token amount or number of cards
    private boolean isClaimed;
    private String cardProbabilitiesJson; // JSON string for custom card probabilities
    
    // Legacy string fields for backward compatibility
    private String title;
    private String description;

    // Primary constructor for Room (with all parameters)
    public Achievement(int id, int titleResId, int descriptionResId, String conditionType, 
                      int conditionValue, String rewardType, int rewardValue, boolean isClaimed,
                      String cardProbabilitiesJson) {
        this.id = id;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
        this.title = null;
        this.description = null;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.isClaimed = isClaimed;
        this.cardProbabilitiesJson = cardProbabilitiesJson;
    }

    // Constructor without custom probabilities - ignored by Room
    @Ignore
    public Achievement(int id, int titleResId, int descriptionResId, String conditionType, 
                      int conditionValue, String rewardType, int rewardValue, boolean isClaimed) {
        this(id, titleResId, descriptionResId, conditionType, conditionValue, rewardType, rewardValue, isClaimed, null);
    }

    // Legacy constructor for backward compatibility - ignored by Room
    @Ignore
    public Achievement(int id, String title, String description, String conditionType, 
                      int conditionValue, String rewardType, int rewardValue, boolean isClaimed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.titleResId = 0; // Not used in legacy mode
        this.descriptionResId = 0; // Not used in legacy mode
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.isClaimed = isClaimed;
        this.cardProbabilitiesJson = null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTitleResId() { return titleResId; }
    public void setTitleResId(int titleResId) { this.titleResId = titleResId; }
    
    public int getDescriptionResId() { return descriptionResId; }
    public void setDescriptionResId(int descriptionResId) { this.descriptionResId = descriptionResId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
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
    
    public String getCardProbabilitiesJson() { return cardProbabilitiesJson; }
    public void setCardProbabilitiesJson(String cardProbabilitiesJson) { this.cardProbabilitiesJson = cardProbabilitiesJson; }
}
