package com.cardmaster.app.ui.boosterselection;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.BoosterRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

public class BoosterSelectionViewModelFactory implements ViewModelProvider.Factory {
    private final BoosterRepository boosterRepository;
    private final OwnedCardRepository ownedCardRepository;

    public BoosterSelectionViewModelFactory(BoosterRepository boosterRepository, OwnedCardRepository ownedCardRepository) {
        this.boosterRepository = boosterRepository;
        this.ownedCardRepository = ownedCardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BoosterSelectionViewModel.class)) {
            return (T) new BoosterSelectionViewModel(boosterRepository, ownedCardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
