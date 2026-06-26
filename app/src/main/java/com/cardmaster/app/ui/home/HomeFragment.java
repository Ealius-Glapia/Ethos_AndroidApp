package com.cardmaster.app.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.BoosterCharge;

import java.util.List;

public class HomeFragment extends Fragment {
    private static final int TIMER_THRESHOLD = 10; // Y: configurable threshold for timer (boosters below this count trigger timer)
    
    private HomeViewModel viewModel;
    private ViewPager2 boosterPager;
    private BoosterAdapter adapter;
    private TextView titleTextView;
    private TextView boosterChargeText;
    private TextView timerText;
    private ImageView timerIcon;
    private TextView noBoosterMessage;
    private Handler timerHandler;
    private Runnable timerRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new HomeViewModelFactory(app.getBoosterRepository()))
                .get(HomeViewModel.class);
        
        titleTextView = view.findViewById(R.id.title_text);
        boosterPager = view.findViewById(R.id.booster_pager);
        boosterChargeText = view.findViewById(R.id.booster_charge_text);
        timerText = view.findViewById(R.id.timer_text);
        timerIcon = view.findViewById(R.id.timer_icon);
        noBoosterMessage = view.findViewById(R.id.no_booster_message);

        setupPager();
        observeBoosters();
        observeBoosterCharge();
        setupTimer();
        
        // Check initial charge state and start timer if needed (using Y threshold)
        BoosterCharge initialCharge = app.getBoosterChargeRepository().getBoosterChargeSync();
        if (initialCharge != null && initialCharge.getCurrentCharge() < TIMER_THRESHOLD) {
            startTimer();
        }
    }

    private void setupPager() {
        adapter = new BoosterAdapter(this::onBoosterClicked);
        boosterPager.setAdapter(adapter);

        boosterPager.setClipToPadding(false);
        boosterPager.setClipChildren(false);
        boosterPager.setOffscreenPageLimit(3);
        boosterPager.setPadding(100, 0, 100, 0);
        boosterPager.setClipToPadding(false);
        boosterPager.setClipChildren(false);

        boosterPager.setPageTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;

            page.setScaleX(scale);
            page.setScaleY(scale);

            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });
    }

    private void observeBoosters() {
        viewModel.getBoosters().observe(getViewLifecycleOwner(), boosters -> {
            if (boosters != null) {
                adapter.submitList(boosters);
                
                if (boosters.isEmpty()) {
                    // No boosters available - hide charge info and show message
                    boosterChargeText.setVisibility(View.GONE);
                    timerIcon.setVisibility(View.GONE);
                    timerText.setVisibility(View.GONE);
                    noBoosterMessage.setVisibility(View.VISIBLE);
                } else {
                    // Boosters available - show charge info and hide message
                    noBoosterMessage.setVisibility(View.GONE);
                    boosterChargeText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void observeBoosterCharge() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        app.getBoosterChargeRepository().getBoosterCharge().observe(getViewLifecycleOwner(), boosterCharge -> {
            if (boosterCharge != null) {
                updateBoosterChargeDisplay(boosterCharge);
            }
        });
    }

    private void updateBoosterChargeDisplay(BoosterCharge boosterCharge) {
        int currentCharge = boosterCharge.getCurrentCharge();
        int maxCharge = boosterCharge.getMaxCharge();

        boosterChargeText.setText(getString(R.string.home_booster_charge, currentCharge, maxCharge));

        // Display in orange if currentCharge exceeds maxCharge
        if (currentCharge > maxCharge) {
            boosterChargeText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            boosterChargeText.setTextColor(getResources().getColor(R.color.md_theme_onSurface));
        }

        // Timer only starts when currentCharge < Y (configurable via TIMER_THRESHOLD)
        if (currentCharge < TIMER_THRESHOLD) {
            timerIcon.setVisibility(View.VISIBLE);
            timerText.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            timerIcon.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
            stopTimer();
        }
    }

    private void setupTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void startTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.post(timerRunnable);
        }
    }

    private void stopTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void updateTimer() {
        CardMasterApplication app = CardMasterApplication.getInstance();
        BoosterCharge boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();
        
        if (boosterCharge != null && boosterCharge.getCurrentCharge() < boosterCharge.getMaxCharge()) {
            long currentTime = System.currentTimeMillis();
            long lastChargeTime = boosterCharge.getLastChargeTime();
            long chargeDuration = boosterCharge.getChargeDurationMs();
            
            long elapsedTime = currentTime - lastChargeTime;
            
            // Calculate how many boosters should have been recharged during elapsed time
            int boostersToRecharge = (int) (elapsedTime / chargeDuration);
            
            if (boostersToRecharge > 0) {
                // Update charge based on elapsed time
                int newCharge = Math.min(boosterCharge.getCurrentCharge() + boostersToRecharge, boosterCharge.getMaxCharge());
                long newLastChargeTime = lastChargeTime + (boostersToRecharge * chargeDuration);
                
                app.getBoosterChargeRepository().updateCharge(newCharge, newLastChargeTime);
                
                // Refresh boosterCharge after update
                boosterCharge = app.getBoosterChargeRepository().getBoosterChargeSync();
            }
            
            // Calculate remaining time for next booster
            long newElapsedTime = currentTime - boosterCharge.getLastChargeTime();
            long remainingTime = chargeDuration - newElapsedTime;
            
            if (remainingTime < 0) {
                remainingTime = 0;
            }
            
            int minutes = (int) (remainingTime / 60000);
            int seconds = (int) ((remainingTime % 60000) / 1000);
            timerText.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void onBoosterClicked(Booster booster) {
        android.util.Log.d("HomeFragment", "Booster clicked: " + booster.getId());
        if (getActivity() instanceof BoosterClickListener) {
            ((BoosterClickListener) getActivity()).onBoosterClicked(booster.getId());
        }
    }

    public interface BoosterClickListener {
        void onBoosterClicked(int boosterId);
    }
}
