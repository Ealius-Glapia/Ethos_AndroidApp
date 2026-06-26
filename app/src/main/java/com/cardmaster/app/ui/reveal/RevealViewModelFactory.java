package com.cardmaster.app.ui.reveal;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.OwnedCardRepository;

public class RevealViewModelFactory implements ViewModelProvider.Factory {
    private final OwnedCardRepository ownedCardRepository;

    public RevealViewModelFactory(OwnedCardRepository ownedCardRepository) {
        this.ownedCardRepository = ownedCardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RevealViewModel.class)) {
            return (T) new RevealViewModel(ownedCardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
