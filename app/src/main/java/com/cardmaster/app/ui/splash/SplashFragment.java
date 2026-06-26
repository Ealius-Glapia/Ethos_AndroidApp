package com.cardmaster.app.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;

public class SplashFragment extends Fragment {
    private SplashViewModel viewModel;
    private TextView logoText;
    private ImageView openingImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        CardMasterApplication app = CardMasterApplication.getInstance();
        viewModel = new ViewModelProvider(this, new SplashViewModelFactory(app.getPreferencesManager()))
                .get(SplashViewModel.class);
        
        logoText = view.findViewById(R.id.logo_text);
        openingImage = view.findViewById(R.id.opening_image);
        
        setupAnimations();
        observeNavigation();
    }

    private void setupAnimations() {
        // Setup opening image - start at full size to show complete image
        openingImage.setScaleX(1.0f);
        openingImage.setScaleY(1.0f);
        openingImage.setAlpha(0f);

        // Ethos text is already visible behind with alpha 0.7
        logoText.setAlpha(0.7f);

        AnimatorSet animatorSet = new AnimatorSet();

        // Image zoom animation - zooming then passing through screen
        ObjectAnimator imageScaleX = ObjectAnimator.ofFloat(openingImage, "scaleX", 1.0f, 15.0f);
        imageScaleX.setDuration(3400);
        imageScaleX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator imageScaleY = ObjectAnimator.ofFloat(openingImage, "scaleY", 1.0f, 15.0f);
        imageScaleY.setDuration(4000);
        imageScaleY.setInterpolator(new DecelerateInterpolator());

        // Fade in quickly at start
        ObjectAnimator imageFadeIn = ObjectAnimator.ofFloat(openingImage, "alpha", 0f, 1f);
        imageFadeIn.setDuration(400);

        // Continuous fade out until completely transparent
        ObjectAnimator imageFadeOut = ObjectAnimator.ofFloat(openingImage, "alpha", 1f, 0f);
        imageFadeOut.setDuration(3000);
        imageFadeOut.setStartDelay(400);

        animatorSet.playTogether(imageScaleX, imageScaleY, imageFadeIn, imageFadeOut);
        animatorSet.start();
    }

    private void observeNavigation() {
        viewModel.getNavigateToLogin().observe(getViewLifecycleOwner(), navigate -> {
            if (navigate) {
                navigateToLogin();
            }
        });

        viewModel.getNavigateToMain().observe(getViewLifecycleOwner(), navigate -> {
            if (navigate) {
                navigateToMain();
            }
        });

        viewModel.checkLoginStatus();
    }

    private void navigateToLogin() {
        // Navigation will be handled by MainActivity
        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).navigateToLogin();
        }
    }

    private void navigateToMain() {
        // Navigation will be handled by MainActivity
        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).navigateToMain();
        }
    }

    public interface NavigationListener {
        void navigateToLogin();
        void navigateToMain();
    }
}
