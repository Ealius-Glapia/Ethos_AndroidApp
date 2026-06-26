package com.cardmaster.app;

import android.app.Application;

import com.cardmaster.app.data.database.AppDatabase;
import com.cardmaster.app.data.database.DatabaseInitializer;
import com.cardmaster.app.data.dao.BoosterChargeDao;
import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.dao.CardDao;
import com.cardmaster.app.data.dao.OwnedCardDao;
import com.cardmaster.app.data.dao.UserCurrencyDao;
import com.cardmaster.app.data.preferences.UserPreferencesManager;
import com.cardmaster.app.data.repository.BoosterChargeRepository;
import com.cardmaster.app.data.repository.BoosterRepository;
import com.cardmaster.app.data.repository.CardRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;
import com.cardmaster.app.data.repository.UserCurrencyRepository;
import com.cardmaster.app.notification.NotificationHelper;
import com.cardmaster.app.work.WorkManagerHelper;

public class CardMasterApplication extends Application {
    private static CardMasterApplication instance;
    private AppDatabase database;
    private BoosterRepository boosterRepository;
    private CardRepository cardRepository;
    private OwnedCardRepository ownedCardRepository;
    private UserCurrencyRepository userCurrencyRepository;
    private BoosterChargeRepository boosterChargeRepository;
    private UserPreferencesManager preferencesManager;
    private DatabaseInitializer databaseInitializer;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        database = AppDatabase.getInstance(this);
        BoosterDao boosterDao = database.boosterDao();
        CardDao cardDao = database.cardDao();
        OwnedCardDao ownedCardDao = database.ownedCardDao();
        UserCurrencyDao userCurrencyDao = database.userCurrencyDao();
        BoosterChargeDao boosterChargeDao = database.boosterChargeDao();

        boosterRepository = new BoosterRepository(boosterDao);
        cardRepository = new CardRepository(cardDao);
        ownedCardRepository = new OwnedCardRepository(ownedCardDao);
        userCurrencyRepository = new UserCurrencyRepository(userCurrencyDao);
        boosterChargeRepository = new BoosterChargeRepository(boosterChargeDao);

        preferencesManager = new UserPreferencesManager(this);
        databaseInitializer = new DatabaseInitializer();

        databaseInitializer.initializeDatabase(this);
        userCurrencyRepository.initializeCurrency();
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this);
        
        // Schedule daily reminder notification
        WorkManagerHelper.scheduleDailyReminder(this);
        
        // Initialize booster charge synchronously
        new Thread(() -> {
            com.cardmaster.app.data.entity.BoosterCharge existing = boosterChargeDao.getBoosterChargeSync();
            if (existing == null) {
                com.cardmaster.app.data.entity.BoosterCharge newCharge = new com.cardmaster.app.data.entity.BoosterCharge();
                boosterChargeDao.insert(newCharge);
                android.util.Log.d("CardMasterApp", "Booster charge initialized");
            } else {
                android.util.Log.d("CardMasterApp", "Booster charge already exists: " + existing.getCurrentCharge() + "/" + existing.getMaxCharge());
            }
        }).start();
    }

    public static CardMasterApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public BoosterRepository getBoosterRepository() {
        return boosterRepository;
    }

    public CardRepository getCardRepository() {
        return cardRepository;
    }

    public OwnedCardRepository getOwnedCardRepository() {
        return ownedCardRepository;
    }

    public UserCurrencyRepository getUserCurrencyRepository() {
        return userCurrencyRepository;
    }

    public BoosterChargeRepository getBoosterChargeRepository() {
        return boosterChargeRepository;
    }

    public UserPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }
}
