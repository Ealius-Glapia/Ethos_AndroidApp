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
import com.cardmaster.app.data.entity.GitHubRepository;

public class EditPasswordDialog extends DialogFragment {

    private static final String ARG_REPOSITORY = "repository";
    
    private EditText passwordEditText;
    private Button updateButton;
    private OnPasswordUpdatedListener listener;
    private GitHubRepository repository;

    public interface OnPasswordUpdatedListener {
        void onPasswordUpdated(String newPassword);
    }

    public static EditPasswordDialog newInstance(GitHubRepository repository) {
        EditPasswordDialog dialog = new EditPasswordDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPOSITORY, repository);
        dialog.setArguments(args);
        return dialog;
    }

    public void setListener(OnPasswordUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_password, null);

        passwordEditText = view.findViewById(R.id.new_password_edit_text);
        updateButton = view.findViewById(R.id.update_password_button);

        if (getArguments() != null) {
            repository = (GitHubRepository) getArguments().getSerializable(ARG_REPOSITORY);
        }

        updateButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButton.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        passwordEditText.addTextChangedListener(textWatcher);

        updateButton.setOnClickListener(v -> {
            String newPassword = passwordEditText.getText().toString();
            if (listener != null) {
                listener.onPasswordUpdated(newPassword);
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}
