package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_currency")
public class UserCurrency {
    @PrimaryKey
    private int id;
    private int tokens;
    private int premiumTokens;

    public UserCurrency() {
        this.id = 1; // Single row for user currency
        this.tokens = 1000; // Default 1,000 tokens
        this.premiumTokens = 0;
    }

    public UserCurrency(int tokens, int premiumTokens) {
        this.id = 1;
        this.tokens = tokens;
        this.premiumTokens = premiumTokens;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public int getPremiumTokens() {
        return premiumTokens;
    }

    public void setPremiumTokens(int premiumTokens) {
        this.premiumTokens = premiumTokens;
    }
}
