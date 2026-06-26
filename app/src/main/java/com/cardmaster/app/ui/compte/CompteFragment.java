package com.cardmaster.app.ui.compte;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.MainActivity;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.UserCurrency;
import com.cardmaster.app.data.preferences.UserPreferencesManager;

import java.util.Locale;

public class CompteFragment extends Fragment {

    private TextView usernameTextView;
    private TextView creditsTextView;
    private RadioGroup languageRadioGroup;
    private UserPreferencesManager preferencesManager;
    private String currentLanguage;

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
        
        loadUsername();
        observeCredits();

        // Load language preference first
        loadLanguagePreference();

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

    public void refreshLanguageUI() {
        // Reload all UI elements with new language
        loadUsername();
    }
}
