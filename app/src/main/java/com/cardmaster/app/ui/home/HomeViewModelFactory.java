package com.cardmaster.app.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.repository.BoosterRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final BoosterRepository boosterRepository;

    public HomeViewModelFactory(BoosterRepository boosterRepository) {
        this.boosterRepository = boosterRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(boosterRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
