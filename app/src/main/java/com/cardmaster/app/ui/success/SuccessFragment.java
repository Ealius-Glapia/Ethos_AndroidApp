package com.cardmaster.app.ui.success;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.MainActivity;
import com.cardmaster.app.R;
import com.cardmaster.app.data.config.CardProbabilityConfig;
import com.cardmaster.app.data.entity.Achievement;
import com.cardmaster.app.data.entity.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuccessFragment extends Fragment {

    private RecyclerView achievementsRecyclerView;
    private AchievementAdapter adapter;
    private CardMasterApplication app;
    private List<Achievement> achievements;
    private Map<Integer, Boolean> conditionMetMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = CardMasterApplication.getInstance();
        
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMain();
            }
        });

        achievementsRecyclerView = view.findViewById(R.id.achievements_recycler_view);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initializeAchievements();
        setupRecyclerView();
    }

    private void initializeAchievements() {
        new Thread(() -> {
            List<Achievement> existingAchievements = app.getAchievementRepository().getAllAchievementsSync();
            
            if (existingAchievements.isEmpty()) {
                // Initialize achievements if they don't exist using resource IDs for translations
                achievements = new ArrayList<>();
                
                // Create card probability configuration for booster rare: one rare card per level 1-9
                // Each card draw has its own probability set
                CardProbabilityConfig boosterRareConfig = new CardProbabilityConfig();
                
                // Card 1: rarity + upgrade = 1
                CardProbabilityConfig.CardDrawConfig draw1 = new CardProbabilityConfig.CardDrawConfig();
                draw1.addProbability(0, 1, 1.0); // upgrade=0, rarity=1, sum=1
                boosterRareConfig.addCardDraw(draw1);
                
                // Card 2: rarity + upgrade = 2
                CardProbabilityConfig.CardDrawConfig draw2 = new CardProbabilityConfig.CardDrawConfig();
                draw2.addProbability(0, 2, 0.5);
                draw2.addProbability(1, 1, 0.5);
                boosterRareConfig.addCardDraw(draw2);
                
                // Card 3: rarity + upgrade = 3
                CardProbabilityConfig.CardDrawConfig draw3 = new CardProbabilityConfig.CardDrawConfig();
                draw3.addProbability(0, 3, 0.3334);
                draw3.addProbability(1, 2, 0.3333);
                draw3.addProbability(2, 1, 0.3333);
                boosterRareConfig.addCardDraw(draw3);
                
                // Card 4: rarity + upgrade = 4
                CardProbabilityConfig.CardDrawConfig draw4 = new CardProbabilityConfig.CardDrawConfig();
                draw4.addProbability(0, 4, 0.25);
                draw4.addProbability(1, 3, 0.25);
                draw4.addProbability(2, 2, 0.25);
                draw4.addProbability(3, 1, 0.25);
                boosterRareConfig.addCardDraw(draw4);
                
                // Card 5: rarity + upgrade = 5
                CardProbabilityConfig.CardDrawConfig draw5 = new CardProbabilityConfig.CardDrawConfig();
                draw5.addProbability(0, 5, 0.2);
                draw5.addProbability(1, 4, 0.2);
                draw5.addProbability(2, 3, 0.2);
                draw5.addProbability(3, 2, 0.2);
                draw5.addProbability(4, 1, 0.2);
                boosterRareConfig.addCardDraw(draw5);
                
                // Card 6: rarity + upgrade = 6
                CardProbabilityConfig.CardDrawConfig draw6 = new CardProbabilityConfig.CardDrawConfig();
                draw6.addProbability(0, 6, 0.2);
                draw6.addProbability(1, 5, 0.2);
                draw6.addProbability(2, 4, 0.2);
                draw6.addProbability(3, 3, 0.2);
                draw6.addProbability(4, 2, 0.2);
                boosterRareConfig.addCardDraw(draw6);
                
                // Card 7: rarity + upgrade = 7
                CardProbabilityConfig.CardDrawConfig draw7 = new CardProbabilityConfig.CardDrawConfig();
                draw7.addProbability(1, 6, 0.25);
                draw7.addProbability(2, 5, 0.25);
                draw7.addProbability(3, 4, 0.25);
                draw7.addProbability(4, 3, 0.25);
                boosterRareConfig.addCardDraw(draw7);
                
                // Card 8: rarity + upgrade = 8
                CardProbabilityConfig.CardDrawConfig draw8 = new CardProbabilityConfig.CardDrawConfig();
                draw8.addProbability(2, 6, 0.3334);
                draw8.addProbability(3, 5, 0.3333);
                draw8.addProbability(4, 4, 0.3333);
                boosterRareConfig.addCardDraw(draw8);
                
                // Card 9: rarity + upgrade = 9
                CardProbabilityConfig.CardDrawConfig draw9 = new CardProbabilityConfig.CardDrawConfig();
                draw9.addProbability(3, 6, 0.5);
                draw9.addProbability(4, 5, 0.5);
                boosterRareConfig.addCardDraw(draw9);
                
                achievements.add(new Achievement(1, 
                    R.string.achievement_10_cards_title,
                    R.string.achievement_10_cards_description,
                    "cards_unlocked", 10, "tokens", 1000, false));
                achievements.add(new Achievement(2,
                    R.string.achievement_100_cards_title,
                    R.string.achievement_100_cards_description,
                    "cards_unlocked", 100, "tokens", 5000, false));
                achievements.add(new Achievement(3,
                    R.string.achievement_500_cards_title,
                    R.string.achievement_500_cards_description,
                    "cards_unlocked", 500, "tokens", 10000, false));
                achievements.add(new Achievement(4,
                    R.string.achievement_all_levels_title,
                    R.string.achievement_all_levels_description,
                    "all_levels_1_9", 0, "booster_custom", 9, false, boosterRareConfig.toJson()));
                
                app.getAchievementRepository().insertAll(achievements);
            } else {
                achievements = existingAchievements;
            }

            // Check conditions for all achievements
            conditionMetMap = new HashMap<>();
            for (Achievement achievement : achievements) {
                conditionMetMap.put(achievement.getId(), checkCondition(achievement));
            }

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.updateAchievements(achievements, conditionMetMap);
                    }
                });
            }
        }).start();
    }

    private void setupRecyclerView() {
        adapter = new AchievementAdapter(achievements != null ? achievements : new ArrayList<>(), 
            conditionMetMap != null ? conditionMetMap : new HashMap<>(),
            this::onClaimClick);
        achievementsRecyclerView.setAdapter(adapter);
    }

    private void onClaimClick(Achievement achievement) {
        new Thread(() -> {
            if (checkCondition(achievement)) {
                if (achievement.getRewardType().equals("tokens")) {
                    // Grant token reward
                    app.getUserCurrencyRepository().addTokens(achievement.getRewardValue());
                    
                    // CHEAT MODE: Don't mark as claimed to allow unlimited claiming
                    // Comment out this line to disable unlimited claiming
                    // app.getAchievementRepository().markAsClaimed(achievement.getId());
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            android.widget.Toast.makeText(getContext(), 
                                "Claimed " + achievement.getRewardValue() + " tokens!", 
                                android.widget.Toast.LENGTH_SHORT).show();
                            refreshAchievements();
                        });
                    }
                } else if (achievement.getRewardType().equals("booster_rare") || achievement.getRewardType().equals("booster_custom")) {
                    // Open booster selection for special reward
                    if (getActivity() instanceof MainActivity) {
                        getActivity().runOnUiThread(() -> {
                            showBoosterSelectionForAchievement(achievement);
                        });
                    }
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(), 
                            "Condition not met yet!", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private boolean checkCondition(Achievement achievement) {
        // CHEAT MODE: Always return true to test claiming
        // Comment out this block to disable cheat mode
        if (true) {
            return true;
        }
        
        if (achievement.getConditionType().equals("cards_unlocked")) {
            int totalCards = app.getOwnedCardRepository().getOwnedCardDao().getTotalUniqueCardsSync();
            return totalCards >= achievement.getConditionValue();
        } else if (achievement.getConditionType().equals("all_levels_1_9")) {
            return hasAllLevels1to9InRare();
        }
        return false;
    }

    private boolean hasAllLevels1to9InRare() {
        // Get all owned cards
        List<Card> ownedCards = app.getOwnedCardRepository().getOwnedCardDao().getOwnedCardsListSync();
        if (ownedCards == null || ownedCards.isEmpty()) {
            return false;
        }

        // Check if we have at least one card of each level 1-9 with rare rarity
        Map<Integer, Card> levelToCard = new HashMap<>();
        for (Card card : ownedCards) {
            try {
                int rarity = Integer.parseInt(card.getRarity());
                int level = card.getNumber();
                // Rare is typically rarity 2 (check your game's rarity system)
                if (rarity == 2 && level >= 1 && level <= 9) {
                    levelToCard.put(level, card);
                }
            } catch (NumberFormatException e) {
                // Skip cards with invalid rarity
            }
        }

        // Check if we have all levels 1-9
        for (int i = 1; i <= 9; i++) {
            if (!levelToCard.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

    private void showBoosterSelectionForAchievement(Achievement achievement) {
        // Show booster selection fragment with achievement context
        com.cardmaster.app.ui.boosterselection.BoosterSelectionFragment fragment = 
            com.cardmaster.app.ui.boosterselection.BoosterSelectionFragment.newInstanceForAchievement(achievement.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void refreshAchievements() {
        new Thread(() -> {
            List<Achievement> updated = app.getAchievementRepository().getAllAchievementsSync();
            
            // Update condition met map
            Map<Integer, Boolean> updatedConditionMetMap = new HashMap<>();
            for (Achievement achievement : updated) {
                updatedConditionMetMap.put(achievement.getId(), checkCondition(achievement));
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    achievements = updated;
                    conditionMetMap = updatedConditionMetMap;
                    adapter.updateAchievements(achievements, conditionMetMap);
                });
            }
        }).start();
    }
}
