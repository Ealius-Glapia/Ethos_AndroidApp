package com.cardmaster.app.ui.compte;

import android.content.Context;
import android.content.res.Configuration;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.MainActivity;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.UserCurrency;
import com.cardmaster.app.data.preferences.UserPreferencesManager;
import com.cardmaster.app.util.UpdateChecker;

import java.util.Locale;

public class CompteFragment extends Fragment {

    private TextView usernameTextView;
    private TextView creditsTextView;
    private RadioGroup languageRadioGroup;
    private CheckBox vibrationCheckbox;
    private Button checkUpdateButton;
    private TextView updateStatusText;
    private UserPreferencesManager preferencesManager;
    private String currentLanguage;
    private UpdateChecker updateChecker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        preferencesManager = app.getPreferencesManager();
        
        usernameTextView = view.findViewById(R.id.username_display);
        creditsTextView = view.findViewById(R.id.credits_display);
        languageRadioGroup = view.findViewById(R.id.language_radio_group);
        vibrationCheckbox = view.findViewById(R.id.vibration_checkbox);
        checkUpdateButton = view.findViewById(R.id.check_update_button);
        updateStatusText = view.findViewById(R.id.update_status_text);

        // Initialize update checker
        updateChecker = new UpdateChecker();

        loadUsername();
        observeCredits();

        // Load language preference first
        loadLanguagePreference();

        // Load vibration preference
        loadVibrationPreference();

        // Set up update check button
        checkUpdateButton.setOnClickListener(v -> checkForUpdates());

        // Set up individual radio button click listeners instead of RadioGroup listener
        // This prevents the listener from triggering on programmatic changes
        RadioButton radioFrench = view.findViewById(R.id.radio_french);
        RadioButton radioEnglish = view.findViewById(R.id.radio_english);

        radioFrench.setOnClickListener(v -> {
            String newLanguage = "fr";
            if (!newLanguage.equals(currentLanguage)) {
                currentLanguage = newLanguage;
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeLanguage(newLanguage);
                }
            }
        });

        radioEnglish.setOnClickListener(v -> {
            String newLanguage = "en";
            if (!newLanguage.equals(currentLanguage)) {
                currentLanguage = newLanguage;
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).changeLanguage(newLanguage);
                }
            }
        });

        // Set up vibration checkbox listener
        vibrationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.saveVibrationEnabled(isChecked);
        });
    }
    
    private void loadUsername() {
        preferencesManager.getUsername(new UserPreferencesManager.UsernameCallback() {
            @Override
            public void onUsernameLoaded(String username) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (username != null && usernameTextView != null) {
                            usernameTextView.setText(username);
                        }
                    });
                }
            }
        });
    }
    
    private void observeCredits() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        app.getUserCurrencyRepository().getUserCurrency().observe(getViewLifecycleOwner(), new Observer<UserCurrency>() {
            @Override
            public void onChanged(UserCurrency userCurrency) {
                if (userCurrency != null && creditsTextView != null) {
                    creditsTextView.setText(String.valueOf(userCurrency.getTokens()));
                }
            }
        });
    }
    
    private void loadLanguagePreference() {
        preferencesManager.getLanguage(new UserPreferencesManager.LanguageCallback() {
            @Override
            public void onLanguageLoaded(String language) {
                currentLanguage = language;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (languageRadioGroup != null) {
                            int checkedId = ("fr".equals(language)) ? R.id.radio_french : R.id.radio_english;
                            languageRadioGroup.check(checkedId);
                        }
                    });
                }
            }
        });
    }

    private void loadVibrationPreference() {
        preferencesManager.getVibrationEnabled(new UserPreferencesManager.VibrationCallback() {
            @Override
            public void onVibrationLoaded(boolean enabled) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (vibrationCheckbox != null) {
                            vibrationCheckbox.setChecked(enabled);
                        }
                    });
                }
            }
        });
    }

    public void refreshLanguageUI() {
        // Reload all UI elements with new language
        loadUsername();
    }

    private void checkForUpdates() {
        // Show checking status
        checkUpdateButton.setEnabled(false);
        updateStatusText.setVisibility(View.VISIBLE);
        updateStatusText.setText(getString(R.string.account_checking_update));

        try {
            // Get current version from package manager
            String currentVersion = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;

            updateChecker.checkForUpdates(currentVersion, new UpdateChecker.UpdateCheckCallback() {
                @Override
                public void onUpdateAvailable(String latestVersion, String downloadUrl) {
                    checkUpdateButton.setEnabled(true);
                    updateStatusText.setText(getString(R.string.account_update_available, latestVersion));

                    // Show toast with option to download
                    if (downloadUrl != null) {
                        Toast.makeText(requireContext(),
                                getString(R.string.account_update_available, latestVersion),
                                Toast.LENGTH_LONG).show();

                        // Open download URL in browser
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                        startActivity(intent);
                    } else {
                        // No direct download link, open releases page
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Ealius-Glapia/Ethos_AndroidApp/releases"));
                        startActivity(intent);
                    }
                }

                @Override
                public void onNoUpdateAvailable() {
                    checkUpdateButton.setEnabled(true);
                    updateStatusText.setText(getString(R.string.account_update_not_available));
                    Toast.makeText(requireContext(),
                            getString(R.string.account_update_not_available),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    checkUpdateButton.setEnabled(true);
                    updateStatusText.setText(getString(R.string.account_update_error));
                    Toast.makeText(requireContext(),
                            getString(R.string.account_update_error),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            checkUpdateButton.setEnabled(true);
            updateStatusText.setText(getString(R.string.account_update_error));
            Toast.makeText(requireContext(),
                    getString(R.string.account_update_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateChecker != null) {
            updateChecker.shutdown();
        }
    }
}
