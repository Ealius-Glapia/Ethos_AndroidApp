package com.cardmaster.app.ui.cardreveal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.Map;

public class CardRevealAdapter extends RecyclerView.Adapter<CardRevealAdapter.CardViewHolder> {
    private List<Card> cards;
    private Map<Integer, Integer> tokenGains; // cardId -> token gain
    private Fragment fragment;

    public CardRevealAdapter(List<Card> cards, Map<Integer, Integer> tokenGains, Fragment fragment) {
        this.cards = cards;
        this.tokenGains = tokenGains;
        this.fragment = fragment;
    }

    public List<Card> getCards() {
        return cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_reveal, parent, false);
        return new CardViewHolder(view, fragment);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.bind(card, tokenGains, cards);
    }

    @Override
    public int getItemCount() {
        return cards != null ? cards.size() : 0;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage;
        private TextView personName;
        private TextView cardRarity;
        private TextView cardUpgrade;
        private LinearLayout tokenGainContainer;
        private TextView tokenGainText;
        private com.google.android.material.card.MaterialCardView cardWrapper;
        private Fragment fragment;
        private List<Card> cards;

        public CardViewHolder(@NonNull View itemView, Fragment fragment) {
            super(itemView);
            this.fragment = fragment;
            cardImage = itemView.findViewById(R.id.card_image);
            personName = itemView.findViewById(R.id.person_name);
            cardRarity = itemView.findViewById(R.id.card_rarity);
            cardUpgrade = itemView.findViewById(R.id.card_upgrade);
            tokenGainContainer = itemView.findViewById(R.id.token_gain_container);
            tokenGainText = itemView.findViewById(R.id.token_gain_text);
            cardWrapper = itemView.findViewById(R.id.card_wrapper);
        }

        public void bind(Card card, Map<Integer, Integer> tokenGains, List<Card> cards) {
            this.cards = cards;
            personName.setText(card.getDescription());
            String rarityLabel = itemView.getContext().getString(R.string.card_rarity);
            String upgradeLabel = itemView.getContext().getString(R.string.card_upgrade);
            cardRarity.setText(rarityLabel + ": " + card.getRarity());
            cardUpgrade.setText(upgradeLabel + ": " + card.getNumber());

            // Load card image from file path (supports webp)
            java.io.File imageFile = new java.io.File(card.getImageUrl());
            android.util.Log.d("CardReveal", "Loading image from: " + card.getImageUrl() + ", exists: " + imageFile.exists());
            com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(imageFile)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(cardImage);

            // Check if card is duplicate (has token gain)
            Integer tokenGain = tokenGains != null ? tokenGains.get(card.getId()) : null;
            boolean isDuplicate = tokenGain != null && tokenGain > 0;

            // Set wrapper stroke color: theme-color for new, gray for duplicate
            if (isDuplicate) {
                cardWrapper.setStrokeColor(itemView.getContext().getColor(android.R.color.darker_gray));
                // Show token gain
                tokenGainContainer.setVisibility(View.VISIBLE);
                tokenGainText.setText("+" + tokenGain);
            } else {
                cardWrapper.setStrokeColor(itemView.getContext().getColor(R.color.md_theme_primary));
                tokenGainContainer.setVisibility(View.GONE);
            }

            // Click to open full screen image
            itemView.setOnClickListener(v -> {
                CardImageDialog dialog = CardImageDialog.newInstance(card, cards, false);
                dialog.show(fragment.getParentFragmentManager(), "card_image_dialog");
            });
        }
    }
}
