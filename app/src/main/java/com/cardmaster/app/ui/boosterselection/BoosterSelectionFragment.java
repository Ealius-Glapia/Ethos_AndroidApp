package com.cardmaster.app.ui.boosterselection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;

public class BoosterSelectionFragment extends Fragment {
    private BoosterSelectionViewModel viewModel;
    private ImageView boosterImage;
    private TextView boosterName;
    private TextView remainingText;
    private TextView completionText;
    private ProgressBar completionBar;
    private View openButton;

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
        
        boosterImage = view.findViewById(R.id.booster_image);
        boosterName = view.findViewById(R.id.booster_name);
        remainingText = view.findViewById(R.id.remaining_text);
        completionText = view.findViewById(R.id.completion_text);
        completionBar = view.findViewById(R.id.completion_bar);
        openButton = view.findViewById(R.id.open_button);
        
        int boosterId = getArguments() != null ? getArguments().getInt("boosterId", 1) : 1;
        viewModel.setBoosterId(boosterId);
        
        observeBooster();
        setupOpenButton();
    }

    private void observeBooster() {
        viewModel.getBooster().observe(getViewLifecycleOwner(), booster -> {
            if (booster != null) {
                displayBooster(booster);
            }
        });
    }

    private void displayBooster(Booster booster) {
        boosterName.setText(booster.getName());
        Glide.with(this)
                .load(booster.getArtworkUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(boosterImage);

        int remaining = 5; // Simulated remaining boosters
        int totalCards = booster.getTotalCards();
        int completionPercent = (int) ((totalCards - remaining * 5) * 100.0 / totalCards);

        remainingText.setText(getString(R.string.booster_selection_remaining, remaining));
        completionText.setText(getString(R.string.booster_selection_completion, completionPercent));
        completionBar.setProgress(completionPercent);
    }

    private void setupOpenButton() {
        openButton.setOnClickListener(v -> {
            if (getActivity() instanceof BoosterOpenListener) {
                ((BoosterOpenListener) getActivity()).onOpenBooster(viewModel.getBoosterId());
            }
        });
    }

    public interface BoosterOpenListener {
        void onOpenBooster(int boosterId);
    }
}
