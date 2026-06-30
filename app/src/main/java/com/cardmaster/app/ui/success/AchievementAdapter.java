package com.cardmaster.app.ui.success;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Achievement;

import java.util.List;
import java.util.Map;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievements;
    private Map<Integer, Boolean> conditionMetMap;
    private OnClaimClickListener claimClickListener;

    public interface OnClaimClickListener {
        void onClaimClick(Achievement achievement);
    }

    public AchievementAdapter(List<Achievement> achievements, Map<Integer, Boolean> conditionMetMap, OnClaimClickListener listener) {
        this.achievements = achievements;
        this.conditionMetMap = conditionMetMap;
        this.claimClickListener = listener;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        boolean conditionMet = conditionMetMap != null && conditionMetMap.containsKey(achievement.getId()) 
                ? conditionMetMap.get(achievement.getId()) : false;
        holder.bind(achievement, conditionMet, claimClickListener);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void updateAchievements(List<Achievement> newAchievements, Map<Integer, Boolean> newConditionMetMap) {
        this.achievements = newAchievements;
        this.conditionMetMap = newConditionMetMap;
        notifyDataSetChanged();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private Button claimButton;
        private View itemView;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            titleTextView = itemView.findViewById(R.id.achievement_title);
            descriptionTextView = itemView.findViewById(R.id.achievement_description);
            claimButton = itemView.findViewById(R.id.claim_button);
        }

        public void bind(Achievement achievement, boolean conditionMet, OnClaimClickListener listener) {
            // Use resource IDs for translations if available, otherwise use legacy string fields
            if (achievement.getTitleResId() != 0) {
                titleTextView.setText(achievement.getTitleResId());
            } else {
                titleTextView.setText(achievement.getTitle());
            }
            
            if (achievement.getDescriptionResId() != 0) {
                descriptionTextView.setText(achievement.getDescriptionResId());
            } else {
                descriptionTextView.setText(achievement.getDescription());
            }

            if (achievement.isClaimed()) {
                claimButton.setText(R.string.claimed);
                claimButton.setEnabled(false);
                itemView.setBackgroundResource(R.drawable.achievement_item_background);
            } else if (conditionMet) {
                claimButton.setText(R.string.claim);
                claimButton.setEnabled(true);
                claimButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onClaimClick(achievement);
                    }
                });
                // Red border when claimable (condition met but not claimed)
                itemView.setBackgroundResource(R.drawable.achievement_item_background_claimable);
            } else {
                claimButton.setText(R.string.claim);
                claimButton.setEnabled(false);
                itemView.setBackgroundResource(R.drawable.achievement_item_background);
            }
        }
    }
}
