package com.cardmaster.app.ui.collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.dao.BoosterWithCards;
import com.cardmaster.app.data.entity.Card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoosterCollectionAdapter extends RecyclerView.Adapter<BoosterCollectionAdapter.BoosterViewHolder> {
    private List<BoosterWithCards> boosterWithCardsList;
    private Map<Integer, Card> ownedCardsMap;
    private Fragment fragment;

    public BoosterCollectionAdapter(List<BoosterWithCards> boosterWithCardsList, Map<Integer, Card> ownedCardsMap, Fragment fragment) {
        this.boosterWithCardsList = boosterWithCardsList;
        this.ownedCardsMap = ownedCardsMap;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public BoosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booster_collection, parent, false);
        return new BoosterViewHolder(view, fragment);
    }

    @Override
    public void onBindViewHolder(@NonNull BoosterViewHolder holder, int position) {
        BoosterWithCards boosterWithCards = boosterWithCardsList.get(position);
        holder.bind(boosterWithCards, ownedCardsMap);
    }

    @Override
    public int getItemCount() {
        return boosterWithCardsList != null ? boosterWithCardsList.size() : 0;
    }

    static class BoosterViewHolder extends RecyclerView.ViewHolder {
        private TextView boosterName;
        private RecyclerView cardsRecyclerView;
        private LinearLayout rarityIconsContainer;
        private ImageView expandIcon;
        private Fragment fragment;
        private boolean isExpanded = false;

        public BoosterViewHolder(@NonNull View itemView, Fragment fragment) {
            super(itemView);
            this.fragment = fragment;
            boosterName = itemView.findViewById(R.id.booster_name);
            cardsRecyclerView = itemView.findViewById(R.id.cards_recycler_view);
            rarityIconsContainer = itemView.findViewById(R.id.rarity_icons_container);
            expandIcon = itemView.findViewById(R.id.expand_icon);

            // Setup click listeners for expand/collapse
            boosterName.setOnClickListener(v -> toggleExpand());
            expandIcon.setOnClickListener(v -> toggleExpand());
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                rarityIconsContainer.setVisibility(View.VISIBLE);
                cardsRecyclerView.setVisibility(View.VISIBLE);
                expandIcon.setRotation(180);
            } else {
                rarityIconsContainer.setVisibility(View.GONE);
                cardsRecyclerView.setVisibility(View.GONE);
                expandIcon.setRotation(0);
            }
        }

        public void bind(BoosterWithCards boosterWithCards, Map<Integer, Card> ownedCardsMap) {
            String displayName = boosterWithCards.booster.getName().replace("_", " ");
            boosterName.setText(displayName);

            List<Card> cards = boosterWithCards.cards;

            // Sort cards by rarity (probability) - lower rarity = higher probability
            cards.sort((c1, c2) -> {
                try {
                    int r1 = Integer.parseInt(c1.getRarity());
                    int r2 = Integer.parseInt(c2.getRarity());
                    return Integer.compare(r1, r2);
                } catch (NumberFormatException e) {
                    return c1.getRarity().compareTo(c2.getRarity());
                }
            });

            CardInBoosterAdapter adapter = new CardInBoosterAdapter(cards, ownedCardsMap, fragment);
            androidx.recyclerview.widget.GridLayoutManager layoutManager = new androidx.recyclerview.widget.GridLayoutManager(itemView.getContext(), 5);
            cardsRecyclerView.setLayoutManager(layoutManager);
            cardsRecyclerView.setAdapter(adapter);

            // Add rarity icons with counts
            addRarityIcons(cards, ownedCardsMap);

            // Reset to collapsed state
            isExpanded = false;
            rarityIconsContainer.setVisibility(View.GONE);
            cardsRecyclerView.setVisibility(View.GONE);
            expandIcon.setRotation(0);
        }

        private void addRarityIcons(List<Card> cards, Map<Integer, Card> ownedCardsMap) {
            rarityIconsContainer.removeAllViews();

            // Calculate counts for each rarity level (rarity + upgrade = icon index) - only for owned cards
            Map<Integer, Integer> rarityCounts = new HashMap<>();
            for (Card card : cards) {
                // Only count if the card is owned
                if (ownedCardsMap.containsKey(card.getId())) {
                    try {
                        int rarity = Integer.parseInt(card.getRarity());
                        int upgrade = card.getNumber();
                        int iconIndex = rarity + upgrade;
                        rarityCounts.put(iconIndex, rarityCounts.getOrDefault(iconIndex, 0) + 1);
                    } catch (NumberFormatException e) {
                        // Skip if rarity is not a number
                    }
                }
            }

            // Add icon and count for each rarity level
            for (Map.Entry<Integer, Integer> entry : rarityCounts.entrySet()) {
                int iconIndex = entry.getKey();
                int count = entry.getValue();

                // Create horizontal layout for icon and count
                LinearLayout iconLayout = new LinearLayout(itemView.getContext());
                iconLayout.setOrientation(LinearLayout.HORIZONTAL);
                iconLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                iconLayoutParams.setMargins(8, 0, 8, 0);
                iconLayout.setLayoutParams(iconLayoutParams);

                // Create ImageView for icon
                ImageView iconView = new ImageView(itemView.getContext());
                int iconResId = getIconResourceId(iconIndex);
                if (iconResId != 0) {
                    iconView.setImageResource(iconResId);
                }
                LinearLayout.LayoutParams iconViewParams = new LinearLayout.LayoutParams(32, 32);
                iconView.setLayoutParams(iconViewParams);
                iconLayout.addView(iconView);

                // Create TextView for count
                TextView countView = new TextView(itemView.getContext());
                countView.setText(": " + count);
                countView.setTextSize(12);
                countView.setTextColor(itemView.getContext().getColor(R.color.md_theme_onSurface));
                LinearLayout.LayoutParams countViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                countViewParams.setMargins(4, 0, 0, 0);
                countView.setLayoutParams(countViewParams);
                iconLayout.addView(countView);

                rarityIconsContainer.addView(iconLayout);
            }
        }

        private int getIconResourceId(int iconIndex) {
            try {
                return itemView.getContext().getResources().getIdentifier(
                        "icon_rarity_" + iconIndex,
                        "drawable",
                        itemView.getContext().getPackageName()
                );
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
