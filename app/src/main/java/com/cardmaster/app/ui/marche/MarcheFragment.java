package com.cardmaster.app.ui.marche;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.BoosterCharge;
import com.cardmaster.app.data.entity.UserCurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarcheFragment extends Fragment {

    private RecyclerView recyclerView;
    private BoosterStackAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marche, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.booster_stack_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Load configuration
        List<BoosterStackConfig> stacks = new ArrayList<>(Arrays.asList(BoosterStackConfig.getDefaultStacks()));
        
        adapter = new BoosterStackAdapter(stacks, this::onStackClick);
        recyclerView.setAdapter(adapter);
    }

    private void onStackClick(BoosterStackConfig stack) {
        CardMasterApplication app = CardMasterApplication.getInstance();
        UserCurrency currency = app.getUserCurrencyRepository().getUserCurrencySync();
        BoosterCharge boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();

        if (currency == null || boosterCharge == null) {
            Toast.makeText(getContext(), getString(R.string.market_error_data_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }

        int currentTokens = currency.getTokens();
        int price = stack.price;
        int boosterCount = stack.count;

        if (currentTokens < price) {
            Toast.makeText(getContext(), getString(R.string.market_insufficient_tokens, currentTokens), Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        showPurchaseConfirmationDialog(stack, currency, boosterCharge);
    }

    private void showPurchaseConfirmationDialog(BoosterStackConfig stack, UserCurrency currency, BoosterCharge boosterCharge) {
        // Use PopupWindow instead of AlertDialog for better z-order control
        android.view.LayoutInflater inflater = (android.view.LayoutInflater) requireContext().getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_market_confirmation, null);
        
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel);
        
        titleTextView.setText(getString(R.string.market_confirm_purchase));
        messageTextView.setText(getString(R.string.market_purchase_message, stack.count, stack.price, currency.getTokens(), boosterCharge.getCurrentCharge(), boosterCharge.getMaxCharge()));
        confirmButton.setText(getString(R.string.market_confirm));
        cancelButton.setText(getString(R.string.market_cancel));
        
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(dialogView, 
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        
        confirmButton.setOnClickListener(v -> {
            executePurchase(stack);
            popupWindow.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> popupWindow.dismiss());
        
        // Show popup at center of screen
        popupWindow.showAtLocation(getView(), android.view.Gravity.CENTER, 0, 0);
    }

    private void executePurchase(BoosterStackConfig stack) {
        CardMasterApplication app = CardMasterApplication.getInstance();
        
        // Deduct tokens
        app.getUserCurrencyRepository().subtractTokens(stack.price);
        
        // Add boosters
        app.getBoosterChargeRepository().addBoosters(stack.count);
        
        Toast.makeText(getContext(), getString(R.string.market_purchase_success, stack.count), Toast.LENGTH_SHORT).show();
    }
}
