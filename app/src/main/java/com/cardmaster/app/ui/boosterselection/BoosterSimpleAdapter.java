package com.cardmaster.app.ui.boosterselection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;

import java.util.ArrayList;
import java.util.List;

public class BoosterSimpleAdapter extends RecyclerView.Adapter<BoosterSimpleAdapter.BoosterViewHolder> {
    
    private List<Booster> boosters = new ArrayList<>();
    private OnBoosterClickListener listener;
    private int selectedBoosterId = -1;

    public interface OnBoosterClickListener {
        void onBoosterClick(Booster booster);
    }

    public void setOnBoosterClickListener(OnBoosterClickListener listener) {
        this.listener = listener;
    }

    public void setBoosters(List<Booster> boosters) {
        this.boosters = boosters;
        notifyDataSetChanged();
    }

    public void setSelectedBoosterId(int boosterId) {
        this.selectedBoosterId = boosterId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BoosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booster_simple, parent, false);
        return new BoosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoosterViewHolder holder, int position) {
        Booster booster = boosters.get(position);
        holder.bind(booster, selectedBoosterId == booster.getId());
    }

    @Override
    public int getItemCount() {
        return boosters.size();
    }

    static class BoosterViewHolder extends RecyclerView.ViewHolder {
        private ImageView boosterImage;
        private TextView boosterName;
        private View selectionIndicator;

        public BoosterViewHolder(@NonNull View itemView) {
            super(itemView);
            boosterImage = itemView.findViewById(R.id.booster_image);
            boosterName = itemView.findViewById(R.id.booster_name);
            selectionIndicator = itemView.findViewById(R.id.selection_indicator);
        }

        public void bind(Booster booster, boolean isSelected) {
            boosterName.setText(booster.getName());
            Glide.with(itemView.getContext())
                    .load(booster.getArtworkUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(boosterImage);

            // Show selection indicator if selected
            if (isSelected) {
                selectionIndicator.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            } else {
                selectionIndicator.setVisibility(View.GONE);
                itemView.setAlpha(0.7f);
            }

            itemView.setOnClickListener(v -> {
                if (itemView.getContext() instanceof OnBoosterClickListener) {
                    ((OnBoosterClickListener) itemView.getContext()).onBoosterClick(booster);
                }
            });
        }
    }
}
