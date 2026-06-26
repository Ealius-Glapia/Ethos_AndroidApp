package com.cardmaster.app.ui.collection;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.BoosterRepository;
import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

public class CollectionViewModelFactory implements ViewModelProvider.Factory {
    private final BoosterRepository boosterRepository;
    private final CardRepository cardRepository;
    private final OwnedCardRepository ownedCardRepository;

    public CollectionViewModelFactory(BoosterRepository boosterRepository, CardRepository cardRepository, 
                                      OwnedCardRepository ownedCardRepository) {
        this.boosterRepository = boosterRepository;
        this.cardRepository = cardRepository;
        this.ownedCardRepository = ownedCardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CollectionViewModel.class)) {
            return (T) new CollectionViewModel(boosterRepository, cardRepository, ownedCardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
