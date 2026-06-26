package com.cardmaster.app.ui.cardreveal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;
import com.github.chrisbanes.photoview.PhotoView;

public class CardImageDialog extends DialogFragment {
    private static final String ARG_CARD = "card";

    private Card card;

    public static CardImageDialog newInstance(Card card) {
        CardImageDialog dialog = new CardImageDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARD, card);
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
        }

        PhotoView cardImageView = view.findViewById(R.id.card_image_full);

        if (card != null && card.getImageUrl() != null) {
            java.io.File imageFile = new java.io.File(card.getImageUrl());
            Glide.with(requireContext())
                    .load(imageFile)
                    .into(cardImageView);
        }

        // Prevent dismiss when clicking on the image
        cardImageView.setOnClickListener(v -> {
            // Do nothing, consume the click
        });

        // Close on background click (outside image)
        view.setOnClickListener(v -> dismiss());
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
