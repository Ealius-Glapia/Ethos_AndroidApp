package com.cardmaster.app.ui.boosteropening;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

public class BoosterOpeningViewModelFactory implements ViewModelProvider.Factory {
    private final CardRepository cardRepository;
    private final OwnedCardRepository ownedCardRepository;

    public BoosterOpeningViewModelFactory(CardRepository cardRepository, OwnedCardRepository ownedCardRepository) {
        this.cardRepository = cardRepository;
        this.ownedCardRepository = ownedCardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BoosterOpeningViewModel.class)) {
            return (T) new BoosterOpeningViewModel(cardRepository, ownedCardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
