package com.cardmaster.app.data.config;

import java.util.ArrayList;
import java.util.List;

public class CardProbabilityConfig {
    private String type;
    private List<CardDrawConfig> cardDraws;

    public CardProbabilityConfig() {
        this.type = "card_draws";
        this.cardDraws = new ArrayList<>();
    }

    public CardProbabilityConfig(String type) {
        this.type = type;
        this.cardDraws = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CardDrawConfig> getCardDraws() {
        return cardDraws;
    }

    public void setCardDraws(List<CardDrawConfig> cardDraws) {
        this.cardDraws = cardDraws;
    }

    public void addCardDraw(CardDrawConfig cardDraw) {
        this.cardDraws.add(cardDraw);
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"type\":\"").append(type).append("\",\"cardDraws\":[");
        
        for (int i = 0; i < cardDraws.size(); i++) {
            CardDrawConfig draw = cardDraws.get(i);
            json.append(draw.toJson());
            
            if (i < cardDraws.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        return json.toString();
    }

    public static class CardDrawConfig {
        private List<CardProbabilityEntry> probabilities;

        public CardDrawConfig() {
            this.probabilities = new ArrayList<>();
        }

        public List<CardProbabilityEntry> getProbabilities() {
            return probabilities;
        }

        public void setProbabilities(List<CardProbabilityEntry> probabilities) {
            this.probabilities = probabilities;
        }

        public void addProbability(int upgrade, int rarity, double probability) {
            this.probabilities.add(new CardProbabilityEntry(upgrade, rarity, probability));
        }

        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{\"probabilities\":[");
            
            for (int i = 0; i < probabilities.size(); i++) {
                CardProbabilityEntry entry = probabilities.get(i);
                json.append("{\"upgrade\":").append(entry.upgrade)
                    .append(",\"rarity\":").append(entry.rarity)
                    .append(",\"probability\":").append(entry.probability).append("}");
                
                if (i < probabilities.size() - 1) {
                    json.append(",");
                }
            }
            
            json.append("]}");
            return json.toString();
        }
    }

    public static class CardProbabilityEntry {
        private int upgrade;
        private int rarity;
        private double probability;

        public CardProbabilityEntry(int upgrade, int rarity, double probability) {
            this.upgrade = upgrade;
            this.rarity = rarity;
            this.probability = probability;
        }

        public int getUpgrade() {
            return upgrade;
        }

        public void setUpgrade(int upgrade) {
            this.upgrade = upgrade;
        }

        public int getRarity() {
            return rarity;
        }

        public void setRarity(int rarity) {
            this.rarity = rarity;
        }

        public double getProbability() {
            return probability;
        }

        public void setProbability(double probability) {
            this.probability = probability;
        }
    }
}
