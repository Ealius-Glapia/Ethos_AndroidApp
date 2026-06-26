package com.cardmaster.app.ui.boosteropening;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.CardRepository;

public class BoosterOpeningViewModelFactory implements ViewModelProvider.Factory {
    private final CardRepository cardRepository;

    public BoosterOpeningViewModelFactory(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BoosterOpeningViewModel.class)) {
            return (T) new BoosterOpeningViewModel(cardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
