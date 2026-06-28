package com.cardmaster.app.ui.boosteropening;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class BoosterOpeningFragment extends Fragment {
    private static final String ARG_BOOSTER_ID = "boosterId";
    
    private BoosterOpeningViewModel viewModel;
    private ImageView packImage;
    private TextView hintTextView;
    private float initialX;
    private float initialY;
    private boolean isOpening = false;
    private CardMasterApplication app;

    public static BoosterOpeningFragment newInstance(int boosterId) {
        BoosterOpeningFragment fragment = new BoosterOpeningFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOSTER_ID, boosterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booster_opening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new BoosterOpeningViewModelFactory(app.getCardRepository(), app.getOwnedCardRepository()))
                .get(BoosterOpeningViewModel.class);
        
        packImage = view.findViewById(R.id.pack_image);
        hintTextView = view.findViewById(R.id.hint_text);
        
        int boosterId = getArguments() != null ? getArguments().getInt(ARG_BOOSTER_ID, 1) : 1;
        viewModel.setBoosterId(boosterId);
        
        loadBoosterImage(boosterId);
        setupPackAnimations();
        setupTouchInteractions();
        observeOpening();
    }

    private void loadBoosterImage(int boosterId) {
        new Thread(() -> {
            try {
                com.cardmaster.app.data.entity.Booster booster = app.getBoosterRepository().getBoosterByIdSync(boosterId);
                if (booster != null) {
                    String imagePath = booster.getArtworkUrl();
                    java.io.File imageFile = new java.io.File(imagePath);

                    // If file doesn't exist at stored path, try the new path
                    if (!imageFile.exists()) {
                        // Try to convert old external path to new files path
                        if (imagePath.contains("/storage/emulated/0/Android/data/")) {
                            String newPath = imagePath.replace("/storage/emulated/0/Android/data/com.cardmaster.app/files", "/data/user/0/com.cardmaster.app/files");
                            imageFile = new java.io.File(newPath);
                            android.util.Log.d("BoosterOpening", "Trying new path: " + newPath);
                        }
                    }

                    java.io.File finalImageFile = imageFile;
                    requireActivity().runOnUiThread(() -> {
                        Glide.with(requireContext())
                                .load(finalImageFile)
                                .dontAnimate()
                                .into(packImage);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.e("BoosterOpening", "Error loading booster image", e);
            }
        }).start();
    }

    private void setupPackAnimations() {
        startBreathingAnimation();
    }

    private void startBreathingAnimation() {
        AnimatorSet breathingSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(packImage, "scaleX", 1.0f, 1.05f, 1.0f);
        scaleX.setDuration(1500);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(packImage, "scaleY", 1.0f, 1.05f, 1.0f);
        scaleY.setDuration(1500);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        breathingSet.playTogether(scaleX, scaleY);
        breathingSet.start();
    }

    private void setupTouchInteractions() {
        packImage.setOnTouchListener((v, event) -> {
            if (isOpening) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();
                    packImage.setScaleX(0.97f);
                    packImage.setScaleY(0.97f);
                    android.util.Log.d("BoosterOpening", "Touch DOWN at: " + initialX + ", " + initialY);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getY() - initialY;
                    float deltaX = event.getX() - initialX;

                    // Check if touch is in top portion of image (first 30%)
                    if (initialY < packImage.getHeight() * 0.3f) {
                        // Detect horizontal swipe (left to right or right to left)
                        if (Math.abs(deltaX) > 100) {
                            android.util.Log.d("BoosterOpening", "Horizontal swipe detected");
                            viewModel.onSwipeGesture();
                            isOpening = true;
                        }
                    } else {
                        // Vertical swipe in other areas
                        if (deltaY < -100) {
                            android.util.Log.d("BoosterOpening", "Vertical swipe detected");
                            viewModel.onSwipeGesture();
                            isOpening = true;
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    packImage.setScaleX(1.0f);
                    packImage.setScaleY(1.0f);
                    android.util.Log.d("BoosterOpening", "Touch UP, tapCount: " + viewModel.tapCount);
                    if (!isOpening) {
                        viewModel.onPackTap();
                    }
                    return true;
            }
            return false;
        });
    }

    private void observeOpening() {
        viewModel.getOpeningComplete().observe(getViewLifecycleOwner(), complete -> {
            android.util.Log.d("BoosterOpening", "Opening complete: " + complete);
            if (complete) {
                startOpeningSequence();
            }
        });
    }

    private void startOpeningSequence() {
        // Vibrate when opening booster (if enabled in settings)
        app.getPreferencesManager().getVibrationEnabled(new com.cardmaster.app.data.preferences.UserPreferencesManager.VibrationCallback() {
            @Override
            public void onVibrationLoaded(boolean enabled) {
                if (enabled) {
                    Vibrator vibrator = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(200);
                        }
                    }
                }
                // Continue animation on UI thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> continueOpeningAnimation());
                }
            }
        });
    }

    private void continueOpeningAnimation() {

        AnimatorSet openingSet = new AnimatorSet();
        
        ObjectAnimator shake = ObjectAnimator.ofFloat(packImage, "rotation", 0f, 10f, -10f, 10f, -10f, 0f);
        shake.setDuration(200);
        
        ObjectAnimator tear = ObjectAnimator.ofFloat(packImage, "scaleX", 1.0f, 1.2f, 0f);
        tear.setDuration(300);
        tear.setStartDelay(200);
        
        openingSet.playSequentially(shake, tear);
        openingSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                packImage.setVisibility(View.GONE);
                hintTextView.setVisibility(View.GONE);
                navigateToReveal();
            }
        });
        openingSet.start();
    }

    private void navigateToReveal() {
        List<Card> cards = viewModel.getRevealedCards().getValue();
        if (cards != null && !cards.isEmpty()) {
            // Navigate to card stack fragment
            CardStackFragment cardStackFragment = CardStackFragment.newInstance(cards);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, cardStackFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public interface BoosterOpenListener {
        void onBoosterOpened(List<Card> cards);
    }
}
