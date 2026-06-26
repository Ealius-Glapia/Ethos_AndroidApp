package com.cardmaster.app.ui.collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.Map;

public class CardInBoosterAdapter extends RecyclerView.Adapter<CardInBoosterAdapter.CardViewHolder> {
    private List<Card> cards;
    private Map<Integer, Card> ownedCardsMap;
    private Fragment fragment;

    public CardInBoosterAdapter(List<Card> cards, Map<Integer, Card> ownedCardsMap, Fragment fragment) {
        this.cards = cards;
        this.ownedCardsMap = ownedCardsMap;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_card, parent, false);
        return new CardViewHolder(view, fragment);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.bind(card, ownedCardsMap);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage;
        private TextView cardInfo;
        private Fragment fragment;

        public CardViewHolder(@NonNull View itemView, Fragment fragment) {
            super(itemView);
            this.fragment = fragment;
            cardImage = itemView.findViewById(R.id.card_image);
            cardInfo = itemView.findViewById(R.id.card_info);
        }

        public void bind(Card card, Map<Integer, Card> ownedCardsMap) {
            Card ownedCard = ownedCardsMap.get(card.getId());
            boolean isOwned = ownedCard != null;

            String rarityShort = itemView.getContext().getString(R.string.card_rarity_short);
            String upgradeShort = itemView.getContext().getString(R.string.card_upgrade_short);
            cardInfo.setText(rarityShort + card.getRarity() + " " + upgradeShort + card.getNumber());

            if (isOwned) {
                // Show owned card image
                cardImage.setAlpha(1.0f);
                java.io.File imageFile = new java.io.File(ownedCard.getImageUrl());
                Glide.with(itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(cardImage);
            } else {
                // Show placeholder for undiscovered card (no alpha)
                cardImage.setImageResource(R.drawable.back_pictures);
            }

            itemView.setOnClickListener(v -> {
                // Only allow opening dialog for owned cards
                if (isOwned) {
                    com.cardmaster.app.ui.cardreveal.CardImageDialog dialog =
                        com.cardmaster.app.ui.cardreveal.CardImageDialog.newInstance(ownedCard);
                    dialog.show(fragment.getParentFragmentManager(), "card_image_dialog");
                }
            });
        }
    }
}
