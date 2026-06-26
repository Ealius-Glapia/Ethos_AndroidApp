package com.cardmaster.app.ui.splash;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.preferences.UserPreferencesManager;

public class SplashViewModelFactory implements ViewModelProvider.Factory {
    private final UserPreferencesManager preferencesManager;

    public SplashViewModelFactory(UserPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SplashViewModel.class)) {
            return (T) new SplashViewModel(preferencesManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
