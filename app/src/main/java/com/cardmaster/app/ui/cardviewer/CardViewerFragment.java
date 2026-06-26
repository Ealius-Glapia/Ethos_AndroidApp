package com.cardmaster.app.ui.cardviewer;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class CardViewerFragment extends Fragment {
    private CardViewerViewModel viewModel;
    private ImageView cardImage;
    private TextView hintTextView;
    private float scaleFactor = 1.0f;
    private float lastTouchX;
    private float lastTouchY;
    private float rotationX = 0f;
    private float rotationY = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new CardViewerViewModelFactory(app.getCardRepository()))
                .get(CardViewerViewModel.class);
        
        cardImage = view.findViewById(R.id.card_image);
        hintTextView = view.findViewById(R.id.hint_text);
        
        int cardId = getArguments() != null ? getArguments().getInt("cardId", 1) : 1;
        viewModel.setCardId(cardId);
        
        setupTouchInteractions();
        observeCard();
    }

    private void setupTouchInteractions() {
        cardImage.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        float deltaX = event.getX() - lastTouchX;
                        float deltaY = event.getY() - lastTouchY;
                        
                        rotationY += deltaX * 0.5f;
                        rotationX -= deltaY * 0.5f;
                        
                        rotationX = Math.max(-30f, Math.min(30f, rotationX));
                        rotationY = Math.max(-30f, Math.min(30f, rotationY));
                        
                        cardImage.setRotationX(rotationX);
                        cardImage.setRotationY(rotationY);
                        
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                    } else if (event.getPointerCount() == 2) {
                        float distance = getDistance(event);
                        float newScaleFactor = distance / 300f;
                        newScaleFactor = Math.max(0.5f, Math.min(3.0f, newScaleFactor));
                        
                        cardImage.setScaleX(newScaleFactor);
                        cardImage.setScaleY(newScaleFactor);
                        scaleFactor = newScaleFactor;
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    resetTilt();
                    return true;
            }
            return false;
        });

        cardImage.setOnClickListener(v -> {
            if (scaleFactor != 1.0f) {
                resetZoom();
            }
        });
    }

    private float getDistance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void resetTilt() {
        ObjectAnimator rotX = ObjectAnimator.ofFloat(cardImage, "rotationX", rotationX, 0f);
        ObjectAnimator rotY = ObjectAnimator.ofFloat(cardImage, "rotationY", rotationY, 0f);
        rotX.setDuration(300);
        rotY.setDuration(300);
        rotX.start();
        rotY.start();
        rotationX = 0f;
        rotationY = 0f;
    }

    private void resetZoom() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardImage, "scaleX", scaleFactor, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardImage, "scaleY", scaleFactor, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
        scaleFactor = 1.0f;
    }

    private void observeCard() {
        viewModel.getCard().observe(getViewLifecycleOwner(), card -> {
            if (card != null) {
                displayCard(card);
            }
        });
    }

    private void displayCard(Card card) {
        Glide.with(this)
                .load(card.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(cardImage);
    }
}
