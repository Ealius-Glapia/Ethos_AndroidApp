package com.cardmaster.app.data.repository;

import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.dao.UserCurrencyDao;
import com.cardmaster.app.data.entity.UserCurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserCurrencyRepository {
    private final UserCurrencyDao userCurrencyDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserCurrencyRepository(UserCurrencyDao userCurrencyDao) {
        this.userCurrencyDao = userCurrencyDao;
    }

    public LiveData<UserCurrency> getUserCurrency() {
        return userCurrencyDao.getUserCurrency();
    }

    public UserCurrency getUserCurrencySync() {
        return userCurrencyDao.getUserCurrencySync();
    }

    public void initializeCurrency() {
        executor.execute(() -> {
            UserCurrency existing = userCurrencyDao.getUserCurrencySync();
            if (existing == null) {
                UserCurrency newCurrency = new UserCurrency(1000, 0); // 1,000 tokens
                userCurrencyDao.insert(newCurrency);
            }
        });
    }

    public void addTokens(int amount) {
        executor.execute(() -> userCurrencyDao.addTokens(amount));
    }

    public void addPremiumTokens(int amount) {
        executor.execute(() -> userCurrencyDao.addPremiumTokens(amount));
    }

    public void subtractTokens(int amount) {
        executor.execute(() -> userCurrencyDao.subtractTokens(amount));
    }

    public void subtractPremiumTokens(int amount) {
        executor.execute(() -> userCurrencyDao.subtractPremiumTokens(amount));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
