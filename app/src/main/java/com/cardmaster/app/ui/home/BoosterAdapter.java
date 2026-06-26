package com.cardmaster.app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;

public class BoosterAdapter extends ListAdapter<Booster, BoosterAdapter.BoosterViewHolder> {
    private final OnBoosterClickListener listener;

    public interface OnBoosterClickListener {
        void onBoosterClick(Booster booster);
    }

    public BoosterAdapter(OnBoosterClickListener listener) {
        super(new DiffUtil.ItemCallback<Booster>() {
            @Override
            public boolean areItemsTheSame(@NonNull Booster oldItem, @NonNull Booster newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Booster oldItem, @NonNull Booster newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public BoosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booster, parent, false);
        return new BoosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoosterViewHolder holder, int position) {
        Booster booster = getItem(position);
        holder.bind(booster, listener);
    }

    static class BoosterViewHolder extends RecyclerView.ViewHolder {
        private ImageView boosterImage;
        private TextView boosterName;

        public BoosterViewHolder(@NonNull View itemView) {
            super(itemView);
            boosterImage = itemView.findViewById(R.id.booster_image);
            boosterName = itemView.findViewById(R.id.booster_name);
        }

        public void bind(Booster booster, OnBoosterClickListener listener) {
            String displayName = booster.getName().replace("_", " ");
            boosterName.setText(displayName);
            
            // Load image from file path
            java.io.File imageFile = new java.io.File(booster.getArtworkUrl());
            if (imageFile.exists()) {
                Glide.with(itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(boosterImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(booster.getArtworkUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(boosterImage);
            }

            itemView.setOnClickListener(v -> listener.onBoosterClick(booster));
            
            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.setScaleX(0.97f);
                        v.setScaleY(0.97f);
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        v.performClick();
                        return true;
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        return true;
                }
                return false;
            });
        }
    }
}
