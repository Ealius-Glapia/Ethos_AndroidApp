package com.cardmaster.app;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.cardmaster.app.databinding.ActivityMainBinding;
import com.cardmaster.app.data.entity.UserCurrency;
import com.cardmaster.app.data.preferences.UserPreferencesManager;
import com.cardmaster.app.ui.ajout.AjoutFragment;
import com.cardmaster.app.ui.compte.CompteFragment;
import com.cardmaster.app.ui.boosteropening.BoosterOpeningFragment;
import com.cardmaster.app.ui.cardreveal.CardRevealFragment;
import com.cardmaster.app.ui.collection.CollectionFragment;
import com.cardmaster.app.ui.favorites.FavoritesFragment;
import com.cardmaster.app.ui.home.HomeFragment;
import com.cardmaster.app.ui.login.LoginFragment;
import com.cardmaster.app.ui.marche.MarcheFragment;
import com.cardmaster.app.ui.splash.SplashFragment;
import com.cardmaster.app.ui.success.SuccessFragment;
import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.work.AlarmScheduler;

import java.util.Locale;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SplashFragment.NavigationListener,
        LoginFragment.NavigationListener,
        HomeFragment.BoosterClickListener,
        BoosterOpeningFragment.BoosterOpenListener,
        CardRevealFragment.CardRevealListener,
        com.cardmaster.app.ui.boosterselection.BoosterSelectionFragment.BoosterOpenListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int ALARM_PERMISSION_REQUEST_CODE = 1002;
    private ActivityMainBinding binding;
    private NavController navController;
    private boolean isMainNavigationVisible = false;
    private TextView tokenCountText;
    private int currentAchievementId = -1; // Track if opening booster for achievement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Request SCHEDULE_EXACT_ALARM permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenCountText = binding.tokenDisplay.tokenCount;
        setupNavigation();
        setupBottomNavigation();
        setupAccountButton();
        setupSuccessButton();
        observeTokenCount();

        if (savedInstanceState == null) {
            showSplashFragment();
        } else {
            // Restore the current tab after recreate
            showBottomNavigation();
            binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
            int currentItemId = binding.bottomNavigation.getSelectedItemId();
            if (currentItemId != 0) {
                binding.bottomNavigation.setSelectedItemId(currentItemId);
            } else {
                // Default to Home if no item selected
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Load language synchronously from SharedPreferences
        android.content.SharedPreferences prefs = newBase.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    private void setupNavigation() {
        // Don't try to find NavHostFragment - we're using manual fragment management
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_marche) {
                showFragment(new MarcheFragment());
                binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
                binding.accountButton.setVisibility(View.VISIBLE);
                binding.successButton.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_home) {
                showFragment(new HomeFragment());
                binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
                binding.accountButton.setVisibility(View.VISIBLE);
                binding.successButton.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.navigation_collection) {
                showFragment(new CollectionFragment());
                binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
                binding.accountButton.setVisibility(View.VISIBLE);
                binding.successButton.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_ajout) {
                showFragment(new AjoutFragment());
                binding.tokenDisplay.getRoot().setVisibility(View.GONE);
                binding.accountButton.setVisibility(View.GONE);
                binding.successButton.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                showFragment(new FavoritesFragment());
                binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
                binding.accountButton.setVisibility(View.VISIBLE);
                binding.successButton.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
    }

    private void setupAccountButton() {
        binding.accountButton.setOnClickListener(v -> {
            showFragment(new CompteFragment());
            binding.tokenDisplay.getRoot().setVisibility(View.GONE);
            binding.accountButton.setVisibility(View.GONE);
            binding.successButton.setVisibility(View.GONE);
        });
    }

    private void setupSuccessButton() {
        binding.successButton.setOnClickListener(v -> {
            showFragment(new SuccessFragment());
            binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
            binding.accountButton.setVisibility(View.GONE);
            binding.successButton.setVisibility(View.GONE);
        });
    }

    private void showSplashFragment() {
        hideBottomNavigation();
        binding.tokenDisplay.getRoot().setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new SplashFragment())
                .commit();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void showBottomNavigation() {
        binding.bottomNavigation.setVisibility(View.VISIBLE);
        isMainNavigationVisible = true;
    }

    private void hideBottomNavigation() {
        binding.bottomNavigation.setVisibility(View.GONE);
        isMainNavigationVisible = false;
    }

    private void observeTokenCount() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        app.getUserCurrencyRepository().getUserCurrency().observe(this, new Observer<UserCurrency>() {
            @Override
            public void onChanged(UserCurrency userCurrency) {
                if (userCurrency != null && tokenCountText != null) {
                    tokenCountText.setText(String.valueOf(userCurrency.getTokens()));
                }
            }
        });
    }

    @Override
    public void navigateToLogin() {
        hideBottomNavigation();
        binding.tokenDisplay.getRoot().setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new LoginFragment())
                .commit();
    }

    @Override
    public void navigateToMain() {
        showBottomNavigation();
        binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
        binding.accountButton.setVisibility(View.VISIBLE);
        binding.successButton.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new HomeFragment())
                .commit();
        // Set selected item to Home to ensure correct tab is shown
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    public void onBoosterClicked(int boosterId) {
        CardMasterApplication app = CardMasterApplication.getInstance();
        com.cardmaster.app.data.entity.BoosterCharge boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();
        
        android.util.Log.d("MainActivity", "Booster clicked. Current charge: " + (boosterCharge != null ? boosterCharge.getCurrentCharge() : "null"));
        
        if (boosterCharge != null && boosterCharge.getCurrentCharge() > 0) {
            binding.tokenDisplay.getRoot().setVisibility(View.GONE);
            BoosterOpeningFragment fragment = BoosterOpeningFragment.newInstance(boosterId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            android.util.Log.d("MainActivity", "Cannot open booster - no charges available");
            android.widget.Toast.makeText(this, getString(R.string.home_no_openings_available), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBoosterOpened(List<Card> cards) {
        // Save cards immediately when booster opens and calculate token gains for duplicates
        android.util.Log.d("MainActivity", "onBoosterOpened - saving " + (cards != null ? cards.size() : 0) + " cards immediately");
        CardMasterApplication app = CardMasterApplication.getInstance();
        long currentTime = System.currentTimeMillis();
        java.util.Map<Integer, Integer> tokenGains = new java.util.HashMap<>();

        // Decrement booster charge only if not an achievement reward
        if (currentAchievementId == -1) {
            app.getBoosterChargeRepository().useBooster();
        }

        for (Card card : cards) {
            // Check if card already exists BEFORE inserting
            int currentQuantity = app.getOwnedCardRepository().getOwnedCardDao().getQuantityByCardIdSync(card.getId());
            boolean isDuplicate = currentQuantity > 0;

            android.util.Log.d("MainActivity", "Saving card with ID: " + card.getId() + ", current quantity: " + currentQuantity + ", isDuplicate: " + isDuplicate);

            com.cardmaster.app.data.entity.OwnedCard ownedCard =
                new com.cardmaster.app.data.entity.OwnedCard(card.getId(), currentTime, 1);
            app.getOwnedCardRepository().insertOwnedCard(ownedCard);

            // If it was a duplicate, calculate and add tokens
            if (isDuplicate) {
                // Calculate token gain: (rarity + upgrade)^4
                try {
                    int rarity = Integer.parseInt(card.getRarity());
                    int upgrade = card.getNumber();
                    int tokenGain = (int) Math.pow(rarity + upgrade, 4);
                    android.util.Log.d("MainActivity", "Duplicate card! Adding " + tokenGain + " tokens for card ID: " + card.getId());
                    app.getUserCurrencyRepository().addTokens(tokenGain);
                    tokenGains.put(card.getId(), tokenGain);
                } catch (NumberFormatException e) {
                    android.util.Log.e("MainActivity", "Error calculating token gain for card ID: " + card.getId());
                }
            }
        }

        // Mark achievement as claimed if this was an achievement reward
        if (currentAchievementId != -1) {
            android.util.Log.d("MainActivity", "Achievement reward completed: " + currentAchievementId);
            // CHEAT MODE: Don't mark as claimed to allow unlimited claiming
            // Comment out this line to disable unlimited claiming
            // app.getAchievementRepository().markAsClaimed(currentAchievementId);
        }

        CardRevealFragment fragment = CardRevealFragment.newInstance(cards, tokenGains);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onContinue(List<Card> cards) {
        // Cards are already saved in onBoosterOpened, navigate based on context
        android.util.Log.d("MainActivity", "onContinue called - currentAchievementId: " + currentAchievementId);
        
        if (currentAchievementId != -1) {
            // Return to achievements screen after achievement reward
            getSupportFragmentManager().popBackStack(); // Go back to booster selection
            getSupportFragmentManager().popBackStack(); // Go back to achievements
            currentAchievementId = -1; // Reset achievement context
            binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
            binding.accountButton.setVisibility(View.GONE);
            binding.successButton.setVisibility(View.GONE);
        } else {
            // Normal flow - navigate back to home
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().popBackStack(); // Go back to home
            binding.tokenDisplay.getRoot().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOpenBooster(int boosterId) {
        // This is called from BoosterSelectionFragment for normal booster opening
        CardMasterApplication app = CardMasterApplication.getInstance();
        com.cardmaster.app.data.entity.BoosterCharge boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();
        
        android.util.Log.d("MainActivity", "Booster clicked. Current charge: " + (boosterCharge != null ? boosterCharge.getCurrentCharge() : "null"));
        
        if (boosterCharge != null && boosterCharge.getCurrentCharge() > 0) {
            binding.tokenDisplay.getRoot().setVisibility(View.GONE);
            BoosterOpeningFragment fragment = BoosterOpeningFragment.newInstance(boosterId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            android.util.Log.d("MainActivity", "Cannot open booster - no charges available");
            android.widget.Toast.makeText(this, getString(R.string.home_no_openings_available), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOpenBoosterForAchievement(int boosterId, int achievementId) {
        currentAchievementId = achievementId;
        CardMasterApplication app = CardMasterApplication.getInstance();
        
        android.util.Log.d("MainActivity", "Achievement booster clicked. Achievement ID: " + achievementId + ", Booster ID: " + boosterId);
        
        // Achievement rewards are free - no charge consumption
        binding.tokenDisplay.getRoot().setVisibility(View.GONE);
        BoosterOpeningFragment fragment = BoosterOpeningFragment.newInstanceForAchievement(boosterId, achievementId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Schedule alarm to check booster charge when app goes to background
        scheduleBoosterChargeCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cancel alarm when app comes to foreground (timer will handle it)
        AlarmScheduler.cancelBoosterChargeCheck(this);
        // Reschedule daily reminder alarm in case permission was granted
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    AlarmScheduler.scheduleDailyReminder(this);
                }
            } else {
                // Android 11 and below don't need this permission
                AlarmScheduler.scheduleDailyReminder(this);
            }
        } catch (SecurityException e) {
            android.util.Log.e("MainActivity", "SecurityException when scheduling alarm: " + e.getMessage());
        }
    }

    private void scheduleBoosterChargeCheck() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        com.cardmaster.app.data.entity.BoosterCharge boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();

        if (boosterCharge != null && boosterCharge.getCurrentCharge() < boosterCharge.getMaxCharge()) {
            // Schedule alarm to check booster charge in 15 minutes
            AlarmScheduler.scheduleBoosterChargeCheck(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public void changeLanguage(String languageCode) {
        // Check if language actually changed
        CardMasterApplication app = CardMasterApplication.getInstance();
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentLang = prefs.getString("language", "en");

        if (languageCode.equals(currentLang)) {
            return; // Language hasn't changed, do nothing
        }

        // Save new language
        app.getPreferencesManager().saveLanguage(languageCode);

        // Recreate activity to apply new language
        recreate();
    }
}
