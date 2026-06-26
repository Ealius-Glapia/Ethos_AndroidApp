package com.cardmaster.app.ui.cardreveal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class CardImageDialog extends DialogFragment {
    private static final String ARG_CARD = "card";
    private static final String ARG_CARDS_LIST = "cards_list";
    private static final String ARG_FROM_COLLECTION = "from_collection";

    private Card card;
    private List<Card> cardsList;
    private boolean fromCollection;
    private PhotoView cardImageView;
    private float initialX;
    private static final float SWIPE_THRESHOLD = 200f;

    public static CardImageDialog newInstance(Card card) {
        CardImageDialog dialog = new CardImageDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARD, card);
        args.putSerializable(ARG_CARDS_LIST, new ArrayList<Card>());
        args.putBoolean(ARG_FROM_COLLECTION, false);
        dialog.setArguments(args);
        return dialog;
    }

    public static CardImageDialog newInstance(Card card, List<Card> cardsList, boolean fromCollection) {
        CardImageDialog dialog = new CardImageDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARD, card);
        args.putSerializable(ARG_CARDS_LIST, new ArrayList<>(cardsList));
        args.putBoolean(ARG_FROM_COLLECTION, fromCollection);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_card_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            card = (Card) getArguments().getSerializable(ARG_CARD);
            cardsList = (List<Card>) getArguments().getSerializable(ARG_CARDS_LIST);
            fromCollection = getArguments().getBoolean(ARG_FROM_COLLECTION, false);
        }

        cardImageView = view.findViewById(R.id.card_image_full);

        if (card != null && card.getImageUrl() != null) {
            java.io.File imageFile = new java.io.File(card.getImageUrl());
            Glide.with(requireContext())
                    .load(imageFile)
                    .into(cardImageView);
        }

        setupSwipeGesture();

        // Prevent dismiss when clicking on the image
        cardImageView.setOnClickListener(v -> {
            // Do nothing, consume the click
        });

        // Close on background click (outside image)
        view.setOnClickListener(v -> dismiss());
    }

    private void setupSwipeGesture() {
        cardImageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    return true;

                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - initialX;

                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX > 0) {
                            // Swipe right - move to previous card (lower sum)
                            moveToPreviousBooster();
                        } else {
                            // Swipe left - move to next card (higher sum)
                            moveToNextBooster();
                        }
                        return true;
                    }
                    return false;
            }
            return false;
        });
    }

    private void moveToPreviousBooster() {
        if (cardsList == null || cardsList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.card_swipe_no_previous, Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove duplicates by card ID and sort by rarity + upgrade sum (ascending)
        List<Card> uniqueCards = new ArrayList<>();
        java.util.Set<Integer> seenIds = new java.util.HashSet<>();
        for (Card c : cardsList) {
            if (seenIds.add(c.getId())) {
                uniqueCards.add(c);
            }
        }

        List<Card> sortedCards = uniqueCards;
        sortedCards.sort((card1, card2) -> {
            int sum1 = Integer.parseInt(card1.getRarity()) + card1.getNumber();
            int sum2 = Integer.parseInt(card2.getRarity()) + card2.getNumber();
            return Integer.compare(sum1, sum2);
        });

        int currentIndex = -1;
        for (int i = 0; i < sortedCards.size(); i++) {
            if (sortedCards.get(i).getId() == card.getId()) {
                currentIndex = i;
                break;
            }
        }

        if (fromCollection) {
            // Skip undiscovered cards
            int newIndex = currentIndex - 1;
            while (newIndex >= 0) {
                Card prevCard = sortedCards.get(newIndex);
                if (isCardDiscovered(prevCard)) {
                    card = prevCard;
                    updateCardImage(prevCard);
                    return;
                }
                newIndex--;
            }
            Toast.makeText(requireContext(), R.string.card_swipe_no_previous, Toast.LENGTH_SHORT).show();
        } else {
            // Navigate in obtained cards only
            if (currentIndex > 0) {
                Card previousCard = sortedCards.get(currentIndex - 1);
                card = previousCard;
                updateCardImage(previousCard);
            } else {
                Toast.makeText(requireContext(), R.string.card_swipe_no_previous, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveToNextBooster() {
        if (cardsList == null || cardsList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.card_swipe_no_next, Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove duplicates by card ID and sort by rarity + upgrade sum (ascending)
        List<Card> uniqueCards = new ArrayList<>();
        java.util.Set<Integer> seenIds = new java.util.HashSet<>();
        for (Card c : cardsList) {
            if (seenIds.add(c.getId())) {
                uniqueCards.add(c);
            }
        }

        List<Card> sortedCards = uniqueCards;
        sortedCards.sort((card1, card2) -> {
            int sum1 = Integer.parseInt(card1.getRarity()) + card1.getNumber();
            int sum2 = Integer.parseInt(card2.getRarity()) + card2.getNumber();
            return Integer.compare(sum1, sum2);
        });

        int currentIndex = -1;
        for (int i = 0; i < sortedCards.size(); i++) {
            if (sortedCards.get(i).getId() == card.getId()) {
                currentIndex = i;
                break;
            }
        }

        if (fromCollection) {
            // Skip undiscovered cards
            int newIndex = currentIndex + 1;
            while (newIndex < sortedCards.size()) {
                Card nextCard = sortedCards.get(newIndex);
                if (isCardDiscovered(nextCard)) {
                    card = nextCard;
                    updateCardImage(nextCard);
                    return;
                }
                newIndex++;
            }
            Toast.makeText(requireContext(), R.string.card_swipe_no_next, Toast.LENGTH_SHORT).show();
        } else {
            // Navigate in obtained cards only
            if (currentIndex >= 0 && currentIndex < sortedCards.size() - 1) {
                Card nextCard = sortedCards.get(currentIndex + 1);
                card = nextCard;
                updateCardImage(nextCard);
            } else {
                Toast.makeText(requireContext(), R.string.card_swipe_no_next, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCardDiscovered(Card card) {
        try {
            CardMasterApplication app = CardMasterApplication.getInstance();
            return app.getOwnedCardRepository().isCardOwnedSync(card.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateCardImage(Card newCard) {
        if (newCard != null && newCard.getImageUrl() != null) {
            java.io.File imageFile = new java.io.File(newCard.getImageUrl());
            Glide.with(requireContext())
                    .load(imageFile)
                    .into(cardImageView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
