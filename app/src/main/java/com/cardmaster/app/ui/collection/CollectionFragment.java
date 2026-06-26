package com.cardmaster.app.ui.collection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.util.List;
import java.util.Map;

public class CollectionFragment extends Fragment {
    private CollectionViewModel viewModel;
    private RecyclerView collectionRecyclerView;
    private BoosterCollectionAdapter adapter;
    private TextView totalCardsText;
    private List<com.cardmaster.app.data.dao.BoosterWithCards> cachedBoosterWithCardsList;
    private Map<Integer, Card> cachedOwnedCardsMap;
    private Integer cachedTotalCards;
    private Integer cachedLoadedCards;
    private android.widget.PopupWindow tooltipPopup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new CollectionViewModelFactory(
                app.getBoosterRepository(),
                app.getCardRepository(),
                app.getOwnedCardRepository()
        )).get(CollectionViewModel.class);

        totalCardsText = view.findViewById(R.id.total_cards_text);
        collectionRecyclerView = view.findViewById(R.id.collection_recycler_view);

        setupRecyclerView();
        observeCollection();
    }

    private void setupRecyclerView() {
        collectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Don't set adapter here - it will be set in updateAdapter when data is ready
    }

    private void observeCollection() {
        viewModel.getBoosterCardsMap().observe(getViewLifecycleOwner(), boosterWithCardsList -> {
            android.util.Log.d("CollectionFragment", "Booster cards map updated: " + (boosterWithCardsList != null ? boosterWithCardsList.size() : 0) + " boosters");
            this.cachedBoosterWithCardsList = boosterWithCardsList;
            android.util.Log.d("CollectionFragment", "Cached boosterWithCardsList: " + (this.cachedBoosterWithCardsList != null ? this.cachedBoosterWithCardsList.size() : "null"));
            updateAdapter();
        });

        viewModel.getOwnedCardsMap().observe(getViewLifecycleOwner(), ownedCardsMap -> {
            android.util.Log.d("CollectionFragment", "Owned cards map updated: " + (ownedCardsMap != null ? ownedCardsMap.size() : 0) + " cards");
            this.cachedOwnedCardsMap = ownedCardsMap;
            android.util.Log.d("CollectionFragment", "Cached ownedCardsMap: " + (this.cachedOwnedCardsMap != null ? this.cachedOwnedCardsMap.size() : "null"));
            updateAdapter();
        });

        viewModel.getTotalUniqueCards().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                cachedTotalCards = total;
                updateTotalCardsDisplay();
            }
        });

        viewModel.getLoadedUniqueCards().observe(getViewLifecycleOwner(), loaded -> {
            if (loaded != null) {
                cachedLoadedCards = loaded;
                updateTotalCardsDisplay();
            }
        });

        totalCardsText.setOnClickListener(v -> showTooltip(v));
    }

    private void updateAdapter() {
        android.util.Log.d("CollectionFragment", "updateAdapter called - cachedBoosterWithCardsList: " + (cachedBoosterWithCardsList != null ? cachedBoosterWithCardsList.size() : "null") + ", cachedOwnedCardsMap: " + (cachedOwnedCardsMap != null ? cachedOwnedCardsMap.size() : "null"));

        if (cachedBoosterWithCardsList != null && cachedOwnedCardsMap != null) {
            android.util.Log.d("CollectionFragment", "Updating adapter with " + cachedBoosterWithCardsList.size() + " boosters and " + cachedOwnedCardsMap.size() + " owned cards");
            adapter = new BoosterCollectionAdapter(cachedBoosterWithCardsList, cachedOwnedCardsMap, this);
            collectionRecyclerView.setAdapter(adapter);
        }
    }

    private void updateTotalCardsDisplay() {
        if (cachedTotalCards != null && cachedLoadedCards != null) {
            totalCardsText.setText(getString(R.string.collection_total, cachedLoadedCards, cachedTotalCards));
        } else if (cachedTotalCards != null) {
            totalCardsText.setText(getString(R.string.collection_total_only, cachedTotalCards));
        }
    }

    private void showTooltip(View anchor) {
        if (cachedTotalCards != null && cachedLoadedCards != null) {
            android.view.LayoutInflater inflater = (android.view.LayoutInflater) requireContext().getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            View tooltipView = inflater.inflate(R.layout.tooltip_layout, null);
            TextView tooltipText = tooltipView.findViewById(R.id.tooltip_text);
            tooltipText.setText(getString(R.string.collection_tooltip, cachedTotalCards, cachedLoadedCards));

            tooltipPopup = new android.widget.PopupWindow(tooltipView, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            tooltipPopup.setOutsideTouchable(true);
            tooltipPopup.setFocusable(true);
            tooltipPopup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

            tooltipPopup.showAsDropDown(anchor, 0, 10);

            tooltipPopup.setOnDismissListener(() -> tooltipPopup = null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }
    }
}
