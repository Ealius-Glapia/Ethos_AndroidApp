package com.cardmaster.app.data.database;

import android.content.Context;

import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.dao.CardDao;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void initializeDatabase(Context context) {
        executor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(context);
            BoosterDao boosterDao = database.boosterDao();
            CardDao cardDao = database.cardDao();

            // Don't initialize with sample data anymore
            // Users should add boosters via the Ajout tab
        });
    }

    private boolean isDatabaseEmpty(BoosterDao boosterDao) {
        return boosterDao.getAllBoosters().getValue() == null || 
               boosterDao.getAllBoosters().getValue().isEmpty();
    }

    private void importFromJson(BoosterDao boosterDao, CardDao cardDao) {
        List<Booster> boosters = createSampleBoosters();
        List<Card> cards = createSampleCards();

        boosterDao.insertAll(boosters);
        cardDao.insertAll(cards);
    }

    private List<Booster> createSampleBoosters() {
        List<Booster> boosters = new ArrayList<>();
        boosters.add(new Booster(1, "Starter Pack", "https://example.com/booster1.jpg", 70, "2024-01-01"));
        boosters.add(new Booster(2, "Fire Expansion", "https://example.com/booster2.jpg", 80, "2024-02-01"));
        boosters.add(new Booster(3, "Water Expansion", "https://example.com/booster3.jpg", 75, "2024-03-01"));
        boosters.add(new Booster(4, "Electric Expansion", "https://example.com/booster4.jpg", 85, "2024-04-01"));
        return boosters;
    }

    private List<Card> createSampleCards() {
        List<Card> cards = new ArrayList<>();
        
        // Starter Pack cards
        for (int i = 1; i <= 70; i++) {
            String rarity = i <= 5 ? "Legendary" : i <= 15 ? "Rare" : i <= 35 ? "Uncommon" : "Common";
            cards.add(new Card(i, 1, "Card " + i, "https://example.com/card1_" + i + ".jpg", rarity, i, "Description for card " + i));
        }
        
        // Fire Expansion cards
        for (int i = 71; i <= 150; i++) {
            String rarity = i <= 80 ? "Legendary" : i <= 100 ? "Rare" : i <= 125 ? "Uncommon" : "Common";
            cards.add(new Card(i, 2, "Card " + i, "https://example.com/card2_" + i + ".jpg", rarity, i - 70, "Description for card " + i));
        }
        
        // Water Expansion cards
        for (int i = 151; i <= 225; i++) {
            String rarity = i <= 160 ? "Legendary" : i <= 180 ? "Rare" : i <= 200 ? "Uncommon" : "Common";
            cards.add(new Card(i, 3, "Card " + i, "https://example.com/card3_" + i + ".jpg", rarity, i - 150, "Description for card " + i));
        }
        
        // Electric Expansion cards
        for (int i = 226; i <= 310; i++) {
            String rarity = i <= 235 ? "Legendary" : i <= 255 ? "Rare" : i <= 280 ? "Uncommon" : "Common";
            cards.add(new Card(i, 4, "Card " + i, "https://example.com/card4_" + i + ".jpg", rarity, i - 225, "Description for card " + i));
        }
        
        return cards;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
