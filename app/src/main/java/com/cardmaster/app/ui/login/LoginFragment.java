package com.cardmaster.app.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;

public class LoginFragment extends Fragment {
    private LoginViewModel viewModel;
    private EditText usernameEditText;
    private Button continueButton;
    private TextView errorTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new LoginViewModelFactory(app.getPreferencesManager()))
                .get(LoginViewModel.class);
        
        usernameEditText = view.findViewById(R.id.username_edit_text);
        continueButton = view.findViewById(R.id.continue_button);
        errorTextView = view.findViewById(R.id.error_text);
        
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                continueButton.setEnabled(s != null && s.length() > 0);
                errorTextView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        continueButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            viewModel.login(username);
        });
    }

    private void observeViewModel() {
        viewModel.getNavigateToMain().observe(getViewLifecycleOwner(), navigate -> {
            if (navigate) {
                navigateToMain();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                errorTextView.setText(error);
                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateToMain() {
        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).navigateToMain();
        }
    }

    public interface NavigationListener {
        void navigateToMain();
    }
}
