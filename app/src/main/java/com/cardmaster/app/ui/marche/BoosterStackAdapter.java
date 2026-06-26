package com.cardmaster.app.ui.marche;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;

import java.util.List;

public class BoosterStackAdapter extends RecyclerView.Adapter<BoosterStackAdapter.ViewHolder> {

    private List<BoosterStackConfig> stacks;
    private final OnStackClickListener listener;

    public interface OnStackClickListener {
        void onStackClick(BoosterStackConfig stack);
    }

    public BoosterStackAdapter(List<BoosterStackConfig> stacks, OnStackClickListener listener) {
        this.stacks = stacks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booster_stack, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BoosterStackConfig stack = stacks.get(position);
        holder.bind(stack, listener);
    }

    @Override
    public int getItemCount() {
        return stacks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout boosterStackContainer;
        private TextView boosterCountTitle;
        private TextView boosterCountText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            boosterStackContainer = itemView.findViewById(R.id.booster_stack_container);
            boosterCountTitle = itemView.findViewById(R.id.booster_count_title);
            boosterCountText = itemView.findViewById(R.id.booster_count_text);
        }

        private int dp(int value) {
            float density = boosterStackContainer.getResources().getDisplayMetrics().density;
            return (int) (value * density);
        }

        public void bind(BoosterStackConfig stack, OnStackClickListener listener) {
            boosterCountTitle.setText(stack.count + " Booster" + (stack.count > 1 ? "s" : ""));
            boosterCountText.setText(String.format("%,d", stack.price));

            // Create stacked back card effect
            createStackedBackCards(stack.count);

            itemView.setOnClickListener(v -> listener.onStackClick(stack));
        }

        private void createStackedBackCards(int count) {
            boosterStackContainer.removeAllViews();

            int cardWidth;
            int cardHeight;

            if (count <= 5) {
                cardWidth = dp(181);
                cardHeight = dp(265);
            } else if (count <= 10) {
                cardWidth = dp(181);
                cardHeight = dp(265);
            } else {
                cardWidth = dp(181);
                cardHeight = dp(265);
            }

            if (count == 10) {
                createPile(5, 100, 20, cardWidth, cardHeight, 30f);   // droite d'abord
                createPile(5, -140, -20, cardWidth, cardHeight, 10f);  // gauche ensuite

            } else if (count == 20) {
                createPile(5, 160, 40, cardWidth, cardHeight, 40f);
                createPile(5, 40, 15, cardWidth, cardHeight, 30f);
                createPile(5, -80, -15, cardWidth, cardHeight, 20f);
                createPile(5, -200, -40, cardWidth, cardHeight, 10f);

            } else {
                createPile(count, 0, 0, cardWidth, cardHeight, 10f);
            }
        }

        private void createPile(int cardsInPile, float pileOffsetX, float pileOffsetY, int cardWidth, int cardHeight, float pileBaseElevation) {
            for (int i = 0; i < cardsInPile; i++) {

                ImageView cardView = new ImageView(boosterStackContainer.getContext());
                cardView.setImageResource(R.drawable.back_pictures);
                cardView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(cardWidth, cardHeight);
                params.gravity = android.view.Gravity.CENTER;
                cardView.setLayoutParams(params);

                // stack effect (profondeur)
                float depthFactor = (cardsInPile - 1 + i);

                float xStack = depthFactor * 10f;
                float yStack = depthFactor * 7f;

                // pile offset (droite/gauche)
                float xPile = pileOffsetX;
                float yPile = pileOffsetY;

                cardView.setTranslationX(xPile + xStack);
                cardView.setTranslationY(yPile + yStack);

                cardView.setElevation(pileBaseElevation + i * 3f);

                cardView.setClipToOutline(true);

                boosterStackContainer.addView(cardView);
            }
        }
    }
}
