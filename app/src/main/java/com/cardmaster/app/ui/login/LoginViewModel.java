package com.cardmaster.app.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.preferences.UserPreferencesManager;

public class LoginViewModel extends ViewModel {
    private final UserPreferencesManager preferencesManager;
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(UserPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String username) {
        if (username == null || username.trim().isEmpty()) {
            errorMessage.postValue("Username cannot be empty");
            return;
        }

        preferencesManager.saveUsername(username.trim());
        navigateToMain.postValue(true);
    }
}
