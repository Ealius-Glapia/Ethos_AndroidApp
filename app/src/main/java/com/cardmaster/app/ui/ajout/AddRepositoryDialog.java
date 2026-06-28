package com.cardmaster.app.ui.ajout;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cardmaster.app.R;

public class AddRepositoryDialog extends DialogFragment {

    private EditText urlEditText;
    private EditText passwordEditText;
    private Button validateButton;
    private OnRepositoryAddedListener listener;
    private String preFilledUrl;

    public interface OnRepositoryAddedListener {
        void onRepositoryAdded(String url, String password);
    }

    public void setListener(OnRepositoryAddedListener listener) {
        this.listener = listener;
    }

    public void setPreFilledUrl(String url) {
        this.preFilledUrl = url;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_repository, null);

        urlEditText = view.findViewById(R.id.url_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        validateButton = view.findViewById(R.id.validate_button);

        if (preFilledUrl != null) {
            urlEditText.setText(preFilledUrl);
        }

        validateButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateButton.setEnabled(isValidInput());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        urlEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        validateButton.setOnClickListener(v -> {
            String url = urlEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (listener != null) {
                listener.onRepositoryAdded(url, password);
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    private boolean isValidInput() {
        String url = urlEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        return !url.isEmpty() && !password.isEmpty() && url.contains("https") && url.contains("github");
    }
}
