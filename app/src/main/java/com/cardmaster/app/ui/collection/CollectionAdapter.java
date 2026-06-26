package com.cardmaster.app.ui.collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

public class CollectionAdapter extends ListAdapter<Card, CollectionAdapter.CardViewHolder> {
    private final OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    public CollectionAdapter(OnCardClickListener listener) {
        super(new DiffUtil.ItemCallback<Card>() {
            @Override
            public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = getItem(position);
        holder.bind(card, listener);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
        }

        public void bind(Card card, OnCardClickListener listener) {
            // Load card image from file path
            java.io.File imageFile = new java.io.File(card.getImageUrl());
            if (imageFile.exists()) {
                Glide.with(itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(cardImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(card.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(cardImage);
            }

            itemView.setOnClickListener(v -> listener.onCardClick(card));
        }
    }
}
