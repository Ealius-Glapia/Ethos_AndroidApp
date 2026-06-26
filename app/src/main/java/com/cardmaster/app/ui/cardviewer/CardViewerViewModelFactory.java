package com.cardmaster.app.ui.cardviewer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.CardRepository;

public class CardViewerViewModelFactory implements ViewModelProvider.Factory {
    private final CardRepository cardRepository;

    public CardViewerViewModelFactory(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CardViewerViewModel.class)) {
            return (T) new CardViewerViewModel(cardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
