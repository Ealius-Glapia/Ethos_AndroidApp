package com.cardmaster.app.ui.ajout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cardmaster.app.R;

public class DeleteConfirmationDialog extends DialogFragment {

    private OnDeleteConfirmedListener listener;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed();
    }

    public void setListener(OnDeleteConfirmedListener listener) {
        this.listener = listener;
    }

    public static DeleteConfirmationDialog newInstance(String repositoryUrl) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        Bundle args = new Bundle();
        args.putString("repository_url", repositoryUrl);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.RoundedDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView messageTextView = view.findViewById(R.id.delete_message_text);
        Button confirmButton = view.findViewById(R.id.confirm_delete_button);
        Button cancelButton = view.findViewById(R.id.cancel_delete_button);

        String repositoryUrl = getArguments() != null ? getArguments().getString("repository_url") : "";
        messageTextView.setText(getString(R.string.delete_repository_confirmation, repositoryUrl));

        confirmButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteConfirmed();
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
