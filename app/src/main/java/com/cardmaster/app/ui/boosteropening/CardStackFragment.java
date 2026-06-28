package com.cardmaster.app.ui.boosteropening;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CardStackFragment extends Fragment {

    private static final String ARG_CARDS = "cards";

    private List<Card> cards;
    private List<View> cardViews;
    private int currentIndex = 0;
    private FrameLayout cardStackContainer;
    private FrameLayout particleContainer;
    private View backgroundView;
    private TextView hintText;
    private boolean isAnimating = false;
    private android.animation.ValueAnimator periodicVibrationAnimator;

    public static CardStackFragment newInstance(List<Card> cards) {
        CardStackFragment fragment = new CardStackFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARDS, new ArrayList<>(cards));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_stack, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardStackContainer = view.findViewById(R.id.card_stack_container);
        particleContainer = view.findViewById(R.id.particle_container);
        backgroundView = view.findViewById(R.id.background);
        hintText = view.findViewById(R.id.hint_text);

        cardStackContainer.setVisibility(View.INVISIBLE);
        particleContainer.setVisibility(View.INVISIBLE);
        backgroundView.setVisibility(View.INVISIBLE);
        hintText.setVisibility(View.INVISIBLE);
        //cards = createDebugCards(); //DEBUG

        if (getArguments() != null) {
            cards = (List<Card>) getArguments().getSerializable(ARG_CARDS);
            //cards = createDebugCards(); //DEBUG
            if (cards != null) {
                sortCardsByRarity();
                cardViews = new ArrayList<>();

                view.post(() -> {
                    createCardStack();
                    updateHint();
                    updateBackground();
                    startParticleEffectsForVisibleCards();
                    triggerVibrationForTopCard();

                    cardStackContainer.setVisibility(View.VISIBLE);
                    particleContainer.setVisibility(View.VISIBLE);
                    backgroundView.setVisibility(View.VISIBLE);
                    hintText.setVisibility(View.VISIBLE);
                });
            }
        }
    }

    private List<Card> createDebugCards() {
        List<Card> debugCards = new ArrayList<>();
        if (cards != null && !cards.isEmpty()) {
            Card templateCard = cards.get(0);
            for (int i = 1; i <= 10; i++) {
                Card debugCard = new Card(
                        templateCard.getId(),
                        templateCard.getBoosterId(),
                        "Debug Level " + i,
                        templateCard.getImageUrl(),
                        String.valueOf(i),
                        0,
                        "Debug card for level " + i
                );
                debugCards.add(debugCard);
            }
        }
        return debugCards;
    }

    private void sortCardsByRarity() {
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                int level1 = Integer.parseInt(c1.getRarity()) + c1.getNumber();
                int level2 = Integer.parseInt(c2.getRarity()) + c2.getNumber();
                return Integer.compare(level1, level2);
            }
        });
    }

    private void createCardStack() {
        cardStackContainer.removeAllViews();

        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            View cardView = createCardView(card);
            cardViews.add(cardView);
            cardStackContainer.addView(cardView);
        }

        Collections.reverse(cardViews);

        if (!cardViews.isEmpty()) {
            setupCardTouch(cardViews.get(0));
        }
    }

    private View createCardView(Card card) {
        FrameLayout wrapper = new FrameLayout(requireContext());
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        wrapper.setBackgroundColor(Color.TRANSPARENT);

        ImageView cardImage = new ImageView(requireContext());
        cardImage.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        cardImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        File imageFile = new File(card.getImageUrl());
        Glide.with(requireContext())
                .load(imageFile)
                .dontAnimate()
                .into(cardImage);

        wrapper.addView(cardImage);
        return wrapper;
    }

    private void updateBackground() {
        if (currentIndex >= cards.size()) {
            backgroundView.setBackgroundColor(Color.WHITE);
            return;
        }

        Card currentCard = cards.get(currentIndex);
        int rarity = Integer.parseInt(currentCard.getRarity());
        int upgrade = currentCard.getNumber();
        int templateLevel = rarity + upgrade;

        GradientDrawable background = createBackgroundDrawable(templateLevel);
        backgroundView.setBackground(background);
    }

    private GradientDrawable createBackgroundDrawable(int templateLevel) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        // On utilise une orientation diagonale (Top-Left à Bottom-Right) pour sublimer les dégradés
        drawable.setOrientation(GradientDrawable.Orientation.TL_BR);

        switch (templateLevel) {
            case 1:
                // Niveau 1 : Blanc pur
                drawable.setColor(Color.WHITE);
                break;
            case 2:
                // Niveau 2 : Rose très clair d'origine (Uni)
                drawable.setColor(Color.parseColor("#FFE8E8"));
                break;
            case 3:
                // Niveau 3 : Premier dégradé simple (Rose clair vers Rose un peu plus soutenu)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#FFE8E8"), Color.parseColor("#FFD0D0")});
                break;
            case 4:
                // Niveau 4 : Dégradé intermédiaire (Rose vers Rouge/Bordeaux doux)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#FFD0D0"), Color.parseColor("#FFA0A0")});
                break;
            case 5:
                // Niveau 5 : Dégradé intense (Rouge vif vers Bordeaux profond)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#FFA0A0"), Color.parseColor("#800020")});
                break;
            case 6:
                // Niveau 6 : Dégradé Premium "Triple" (Bordeaux -> Rose Éclatant -> Bordeaux)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#800020"), Color.parseColor("#FFA0A0"), Color.parseColor("#800020")});
                break;
            case 7:
                // Niveau 7 : Arc-en-ciel conservé
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{
                        Color.RED, Color.parseColor("#FF7F00"), Color.YELLOW,
                        Color.GREEN, Color.BLUE, Color.parseColor("#4B0082"), Color.parseColor("#9400D3")
                });
                break;
            case 8:
                // Niveau 9 : Fond BRONZE (Particules Silver #C0C0C0 par-dessus -> Très visible !)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#A36627"), Color.parseColor("#E5A65D"), Color.parseColor("#A36627")});
                break;
            case 9:
                // Niveau 10 : Fond SILVER (Particules Gold #FFD700 par-dessus -> Contraste maximal pour le niveau max !)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#9C9C9C"), Color.parseColor("#E0E0E0"), Color.parseColor("#7A7A7A")});
                break;
            case 10:
                // Niveau 8 : Fond OR (Particules Bronze #CD7F32 par-dessus -> Très visible !)
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                drawable.setColors(new int[]{Color.parseColor("#D4AF37"), Color.parseColor("#FFF3A8"), Color.parseColor("#AA7C11")});
                break;
            default:
                drawable.setColor(Color.WHITE);
        }
        return drawable;
    }

    private void startParticleEffectsForVisibleCards() {
        particleContainer.removeAllViews();

        if (currentIndex >= cards.size() || cardViews.isEmpty()) {
            return;
        }

        Card currentCard = cards.get(currentIndex);
        int rarity = Integer.parseInt(currentCard.getRarity());
        int upgrade = currentCard.getNumber();
        int templateLevel = rarity + upgrade;

        if (templateLevel == 8 || templateLevel == 9 || templateLevel == 10) {
            View currentCardView = cardViews.get(0);
            startParticleEffect(currentCardView, templateLevel);
        }
    }

    private void startParticleEffect(View cardView, int level) {
        if (level < 8 || level > 10) return;
        animateParticles(cardView, level);
    }

    private void animateParticles(View cardView, int level) {
        // 1. Augmentation significative du nombre maximal de particules
        final int maxParticles = (level == 8) ? 150 : (level == 9) ? 250 : 450;

        // 2. EFFET "BURST" IMMÉDIAT : Génère des particules instantanément à l'apparition
        // Moitié sur le point de départ extérieur pour éliminer le délai visuel
        int initialBurstCount = maxParticles / 3;
        for (int i = 0; i < initialBurstCount; i++) {
            createSingleParticle(cardView, level, true);
        }

        // 3. Système de flux continu (Spawn accéléré à 30ms pour plus de densité)
        Runnable spawnRunnable = new Runnable() {
            @Override
            public void run() {
                if (cardView.getParent() == null || currentIndex >= cards.size()) {
                    return;
                }

                if (particleContainer.getChildCount() < maxParticles) {
                    createSingleParticle(cardView, level, false);
                }

                particleContainer.postDelayed(this, 30);
            }
        };

        particleContainer.post(spawnRunnable);
    }

    private float[] getCardCenterInParticleContainer(View cardView) {
        int[] location = new int[2];
        cardView.getLocationOnScreen(location);
        int[] containerLocation = new int[2];
        particleContainer.getLocationOnScreen(containerLocation);

        float centerX = location[0] - containerLocation[0] + cardView.getWidth() / 2f;
        float centerY = location[1] - containerLocation[1] + cardView.getHeight() / 2f;
        return new float[]{centerX, centerY};
    }

    private void createSingleParticle(View cardView, int level, boolean isInitialBurst) {
        final View particle = new View(requireContext());
        int size = (level == 8) ? 10 : (level == 9) ? 14 : 18;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        particle.setLayoutParams(params);

        // FORMULAIRE DES PARTICULES : Transformation en Cercles Parfaits
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        String particleColor = (level == 10) ? "#CD7F32" : (level == 8 ? "#C0C0C0" : "#FFD700");
        circleDrawable.setColor(Color.parseColor(particleColor));
        particle.setBackground(circleDrawable);
        particle.setAlpha(1.0f);

        particleContainer.addView(particle);

        final float angle = (float) (Math.random() * 2 * Math.PI);
        final float distance = 1000 + (float) (Math.random() * 300);
        final long duration = 2000 + (long) (Math.random() * 500);

        particle.postDelayed(() -> {
            float[] cardCenter = getCardCenterInParticleContainer(cardView);
            float startX = cardCenter[0] - size / 2f;
            float startY = cardCenter[1] - size / 2f;

            // Si c'est le burst initial, on décale directement l'origine vers les bords de la carte
            if (isInitialBurst) {
                float offset = 250f + (float) (Math.random() * 150f);
                startX += (float) (Math.cos(angle) * offset);
                startY += (float) (Math.sin(angle) * offset);
            }

            particle.setX(startX);
            particle.setY(startY);

            float endX = startX + (float) (Math.cos(angle) * distance);
            float endY = startY + (float) (Math.sin(angle) * distance);

            AnimatorSet anim = new AnimatorSet();
            anim.playTogether(
                    ObjectAnimator.ofFloat(particle, "x", endX),
                    ObjectAnimator.ofFloat(particle, "y", endY)
            );

            anim.setDuration(isInitialBurst ? (long) (duration * 0.7) : duration);
            anim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    particleContainer.removeView(particle);
                    if (cardView.getParent() != null && currentIndex < cards.size()) {
                        createSingleParticle(cardView, level, false);
                    }
                }
            });
            anim.start();
        }, 0);
    }

    // GESTION DES VIBRATIONS : Cycle rapide et agressif (Niv 10) vs Standard (Niv 9)
    private void triggerVibrationForTopCard() {
        if (periodicVibrationAnimator != null) {
            periodicVibrationAnimator.cancel();
            periodicVibrationAnimator = null;
        }

        if (currentIndex < cards.size() && !cardViews.isEmpty()) {
            Card currentCard = cards.get(currentIndex);
            int rarity = Integer.parseInt(currentCard.getRarity());
            int upgrade = currentCard.getNumber();
            int templateLevel = rarity + upgrade;

            if (templateLevel == 9 || templateLevel == 10) {
                View topCardView = cardViews.get(0);

                // Configuration selon le palier d'intensité
                final long loopDuration = (templateLevel == 10) ? 1400 : 3000; // Fréquence beaucoup plus haute pour le niveau 10 (1.4s vs 3s)
                final float activeWindow = (templateLevel == 10) ? 0.35f : 0.13f; // Fenêtre d'oscillation active prolongée
                final float shakeIntensity = (templateLevel == 10) ? 45f : 20f; // Amplitude violente (45px) vs douce (20px)
                final double frequencyMultiplier = (templateLevel == 10) ? 9.0 : 5.0; // Oscillations plus nerveuses

                periodicVibrationAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f);
                periodicVibrationAnimator.setDuration(loopDuration);
                periodicVibrationAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);

                periodicVibrationAnimator.addUpdateListener(animation -> {
                    float fraction = animation.getAnimatedFraction();

                    if (fraction < activeWindow) {
                        float subFraction = fraction / activeWindow;
                        // Sinus amorti calculé d'après les nouvelles configurations dynamiques
                        float shake = (float) (Math.sin(subFraction * Math.PI * frequencyMultiplier) * shakeIntensity * (1f - subFraction));
                        topCardView.setTranslationX(shake);
                    } else {
                        topCardView.setTranslationX(0f);
                    }
                });

                periodicVibrationAnimator.start();
            }
        }
    }

    private void setupCardTouch(View cardView) {
        cardView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX;
            private float initialY;
            private boolean hasSwiped = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isAnimating) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        initialY = event.getY();
                        hasSwiped = false;
                        cardView.setScaleX(0.95f);
                        cardView.setScaleY(0.95f);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (hasSwiped) return true;

                        float moveDeltaX = event.getX() - initialX;
                        float moveDeltaY = event.getY() - initialY;

                        if (Math.abs(moveDeltaX) > 50 || Math.abs(moveDeltaY) > 50) {
                            hasSwiped = true;
                            swipeCard(cardView, moveDeltaX > 0 ? 1 : -1);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cardView.setScaleX(1.0f);
                        cardView.setScaleY(1.0f);

                        if (!hasSwiped) {
                            float upDeltaX = event.getX() - initialX;
                            float upDeltaY = event.getY() - initialY;
                            if (Math.abs(upDeltaX) < 20 && Math.abs(upDeltaY) < 20) {
                                swipeCard(cardView, -1);
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void swipeCard(View cardView, int direction) {
        if (isAnimating) return;
        isAnimating = true;

        float targetX = direction * cardStackContainer.getWidth();

        AnimatorSet swipeSet = new AnimatorSet();
        swipeSet.playTogether(
                ObjectAnimator.ofFloat(cardView, "translationX", targetX),
                ObjectAnimator.ofFloat(cardView, "rotation", direction * 15f),
                ObjectAnimator.ofFloat(cardView, "alpha", 0f)
        );

        swipeSet.setDuration(300);
        swipeSet.setInterpolator(new AccelerateInterpolator());

        swipeSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (periodicVibrationAnimator != null) {
                    periodicVibrationAnimator.cancel();
                }

                cardStackContainer.removeView(cardView);
                cardViews.remove(cardView);
                currentIndex++;
                updateHint();
                updateBackground();
                startParticleEffectsForVisibleCards();
                triggerVibrationForTopCard();
                isAnimating = false;

                if (currentIndex < cards.size()) {
                    if (!cardViews.isEmpty()) {
                        setupCardTouch(cardViews.get(0));
                    }
                } else {
                    navigateToCollectionView();
                }
            }
        });
        swipeSet.start();
    }

    private void updateHint() {
        if (hintText != null) {
            if (currentIndex == cards.size() - 1) {
                hintText.setText("Tap or swipe to view collection");
            } else {
                hintText.setText("Tap or swipe to reveal next card");
            }
        }
    }

    private void navigateToCollectionView() {
        requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (getActivity() instanceof BoosterOpeningFragment.BoosterOpenListener) {
            ((BoosterOpeningFragment.BoosterOpenListener) getActivity()).onBoosterOpened(cards);
        }
    }
}