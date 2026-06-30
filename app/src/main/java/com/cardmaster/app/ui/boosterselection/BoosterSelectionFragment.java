package com.cardmaster.app.ui.boosterselection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.ui.home.BoosterAdapter;

import java.util.ArrayList;
import java.util.List;

public class BoosterSelectionFragment extends Fragment {
    private static final String ARG_BOOSTER_ID = "boosterId";
    private static final String ARG_ACHIEVEMENT_ID = "achievementId";
    
    private BoosterSelectionViewModel viewModel;
    private TextView boosterName;
    private TextView remainingText;
    private TextView completionText;
    private ProgressBar completionBar;
    private View openButton;
    private ViewPager2 boosterPager;
    private BoosterAdapter adapter;
    private int achievementId;
    private Booster selectedBooster;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booster_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new BoosterSelectionViewModelFactory(
                app.getBoosterRepository(),
                app.getOwnedCardRepository()
        )).get(BoosterSelectionViewModel.class);
        
        boosterPager = view.findViewById(R.id.booster_pager);
        boosterName = view.findViewById(R.id.booster_name);
        remainingText = view.findViewById(R.id.remaining_text);
        completionText = view.findViewById(R.id.completion_text);
        completionBar = view.findViewById(R.id.completion_bar);
        openButton = view.findViewById(R.id.open_button);
        
        int boosterId = getArguments() != null ? getArguments().getInt(ARG_BOOSTER_ID, 1) : 1;
        achievementId = getArguments() != null ? getArguments().getInt(ARG_ACHIEVEMENT_ID, -1) : -1;
        viewModel.setBoosterId(boosterId);
        
        if (achievementId != -1) {
            // Achievement mode: show all boosters like home screen
            setupBoosterPager();
            showAllBoosters();
        } else {
            // Normal mode: show single booster
            observeBooster();
        }
        setupOpenButton();
    }

    public static BoosterSelectionFragment newInstance(int boosterId) {
        BoosterSelectionFragment fragment = new BoosterSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOSTER_ID, boosterId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BoosterSelectionFragment newInstanceForAchievement(int achievementId) {
        BoosterSelectionFragment fragment = new BoosterSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACHIEVEMENT_ID, achievementId);
        fragment.setArguments(args);
        return fragment;
    }

    private void observeBooster() {
        viewModel.getBooster().observe(getViewLifecycleOwner(), booster -> {
            if (booster != null) {
                selectedBooster = booster;
                displayBoosterInfo(booster);
            }
        });
    }

    private void setupBoosterPager() {
        adapter = new BoosterAdapter(this::onBoosterClicked);
        boosterPager.setAdapter(adapter);

        boosterPager.setClipToPadding(false);
        boosterPager.setClipChildren(false);
        boosterPager.setOffscreenPageLimit(3);
        boosterPager.setPadding(100, 0, 100, 0);
        boosterPager.setClipToPadding(false);
        boosterPager.setClipChildren(false);

        boosterPager.setPageTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });
    }

    private void showAllBoosters() {
        new Thread(() -> {
            CardMasterApplication app = CardMasterApplication.getInstance();
            List<Booster> allBoosters = app.getBoosterRepository().getAllBoostersSync();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (allBoosters != null && !allBoosters.isEmpty()) {
                        // Select the first booster by default
                        selectedBooster = allBoosters.get(0);
                        displayBoosterInfo(selectedBooster);
                        adapter.submitList(new ArrayList<>(allBoosters));
                    }
                });
            }
        }).start();
    }

    private void onBoosterClicked(Booster booster) {
        selectedBooster = booster;
        displayBoosterInfo(booster);
    }

    private void displayBoosterInfo(Booster booster) {
        // Replace underscores with spaces in booster name
        String displayName = booster.getName().replace("_", " ");
        boosterName.setText(displayName);

        if (achievementId != -1) {
            // Achievement mode: hide irrelevant displays
            remainingText.setVisibility(View.GONE);
            completionText.setVisibility(View.GONE);
            completionBar.setVisibility(View.GONE);
        } else {
            // Normal mode: show completion info
            int remaining = 5; // Simulated remaining boosters
            int totalCards = booster.getTotalCards();
            int completionPercent = (int) ((totalCards - remaining * 5) * 100.0 / totalCards);

            remainingText.setText(getString(R.string.booster_selection_remaining, remaining));
            completionText.setText(getString(R.string.booster_selection_completion, completionPercent));
            completionBar.setProgress(completionPercent);
        }
    }

    private void setupOpenButton() {
        openButton.setOnClickListener(v -> {
            if (getActivity() instanceof BoosterOpenListener) {
                if (achievementId != -1) {
                    // Achievement reward opening - use selected booster
                    if (selectedBooster != null) {
                        ((BoosterOpenListener) getActivity()).onOpenBoosterForAchievement(selectedBooster.getId(), achievementId);
                    }
                } else {
                    // Normal opening
                    ((BoosterOpenListener) getActivity()).onOpenBooster(viewModel.getBoosterId());
                }
            }
        });
    }

    public interface BoosterOpenListener {
        void onOpenBooster(int boosterId);
        void onOpenBoosterForAchievement(int boosterId, int achievementId);
    }
}
