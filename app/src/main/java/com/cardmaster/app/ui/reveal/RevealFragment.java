package com.cardmaster.app.ui.reveal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.util.List;

public class RevealFragment extends Fragment {
    private RevealViewModel viewModel;
    private ImageView cardImage;
    private ImageView cardBack;
    private TextView hintTextView;
    private int currentIndex = 0;
    private List<Card> cards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reveal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new RevealViewModelFactory(app.getOwnedCardRepository()))
                .get(RevealViewModel.class);
        
        cardImage = view.findViewById(R.id.card_image);
        cardBack = view.findViewById(R.id.card_back);
        hintTextView = view.findViewById(R.id.hint_text);
        
        Bundle args = getArguments();
        if (args != null) {
            cards = (List<Card>) args.getSerializable("cards");
            if (cards != null) {
                viewModel.setCards(cards);
                setupCardDisplay();
            }
        }
        
        observeViewModel();
    }

    private void setupCardDisplay() {
        if (cards != null && !cards.isEmpty()) {
            showCardBack();
        }
    }

    private void showCardBack() {
        cardImage.setVisibility(View.GONE);
        cardBack.setVisibility(View.VISIBLE);
        cardBack.setOnClickListener(v -> flipCard());
    }

    private void flipCard() {
        if (currentIndex >= cards.size()) return;
        
        Card card = cards.get(currentIndex);
        
        AnimatorSet flipSet = new AnimatorSet();
        
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(cardBack, "rotationY", 0f, 90f);
        rotationY.setDuration(225);
        rotationY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        rotationY.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                cardBack.setVisibility(View.GONE);
                cardImage.setVisibility(View.VISIBLE);
                cardImage.setRotationY(-90f);
                
                Glide.with(RevealFragment.this)
                        .load(card.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(cardImage);
                
                ObjectAnimator revealRotation = ObjectAnimator.ofFloat(cardImage, "rotationY", -90f, 0f);
                revealRotation.setDuration(225);
                revealRotation.setInterpolator(new AccelerateDecelerateInterpolator());
                revealRotation.start();
                
                if (card.getRarity().equals("Legendary")) {
                    addRareEffects();
                }
            }
        });
        
        flipSet.start();
        
        viewModel.revealCard(currentIndex);
        currentIndex++;
        
        if (currentIndex < cards.size()) {
            hintTextView.setText(R.string.reveal_tap_hint);
        } else {
            hintTextView.setText("All cards revealed!");
            cardImage.setOnClickListener(null);
        }
    }

    private void addRareEffects() {
        cardImage.setElevation(20f);
        ObjectAnimator glow = ObjectAnimator.ofFloat(cardImage, "alpha", 0.8f, 1.0f, 0.8f);
        glow.setDuration(500);
        glow.setRepeatCount(3);
        glow.start();
    }

    private void observeViewModel() {
        viewModel.getAllRevealed().observe(getViewLifecycleOwner(), revealed -> {
            if (revealed) {
                hintTextView.postDelayed(() -> {
                    if (getActivity() instanceof RevealCompleteListener) {
                        ((RevealCompleteListener) getActivity()).onRevealComplete();
                    }
                }, 2000);
            }
        });
    }

    public interface RevealCompleteListener {
        void onRevealComplete();
    }
}
