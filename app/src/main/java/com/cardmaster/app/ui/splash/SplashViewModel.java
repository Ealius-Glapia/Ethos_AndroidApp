package com.cardmaster.app.ui.splash;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.preferences.UserPreferencesManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SplashViewModel extends ViewModel {
    private final UserPreferencesManager preferencesManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();

    public SplashViewModel(UserPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public void checkLoginStatus() {
        executor.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(3800); //3400 + 400
                
                if (preferencesManager.hasUsername()) {
                    navigateToMain.postValue(true);
                } else {
                    navigateToLogin.postValue(true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                navigateToLogin.postValue(true);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
