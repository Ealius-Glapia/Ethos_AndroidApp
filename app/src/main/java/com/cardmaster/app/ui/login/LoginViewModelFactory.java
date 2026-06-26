package com.cardmaster.app.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.data.preferences.UserPreferencesManager;

public class LoginViewModelFactory implements ViewModelProvider.Factory {
    private final UserPreferencesManager preferencesManager;

    public LoginViewModelFactory(UserPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(preferencesManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
