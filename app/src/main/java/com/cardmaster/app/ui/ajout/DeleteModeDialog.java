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

public class DeleteModeDialog extends DialogFragment {

    public enum DeleteMode {
        HARD,
        SOFT
    }

    private OnDeleteModeSelectedListener listener;
    private String itemName;
    private boolean imagesAlreadyDeleted;

    public interface OnDeleteModeSelectedListener {
        void onDeleteModeSelected(DeleteMode mode);
    }

    public void setListener(OnDeleteModeSelectedListener listener) {
        this.listener = listener;
    }

    public static DeleteModeDialog newInstance(String itemName, boolean imagesAlreadyDeleted) {
        DeleteModeDialog dialog = new DeleteModeDialog();
        Bundle args = new Bundle();
        args.putString("itemName", itemName);
        args.putBoolean("imagesAlreadyDeleted", imagesAlreadyDeleted);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_mode, null);

        TextView titleTextView = view.findViewById(R.id.delete_mode_title);
        Button hardDeleteButton = view.findViewById(R.id.hard_delete_button);
        Button softDeleteButton = view.findViewById(R.id.soft_delete_button);
        Button cancelButton = view.findViewById(R.id.cancel_delete_mode_button);

        if (getArguments() != null) {
            itemName = getArguments().getString("itemName");
            imagesAlreadyDeleted = getArguments().getBoolean("imagesAlreadyDeleted", false);
        }

        titleTextView.setText(getString(R.string.delete_mode_title) + "\n" + itemName);

        // Disable soft delete if images are already deleted
        if (imagesAlreadyDeleted) {
            softDeleteButton.setEnabled(false);
            softDeleteButton.setAlpha(0.5f);
        }

        hardDeleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteModeSelected(DeleteMode.HARD);
            }
            dismiss();
        });

        softDeleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteModeSelected(DeleteMode.SOFT);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
