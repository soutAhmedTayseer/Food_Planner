package com.example.food_planner.onboarding.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food_planner.R;
import com.example.food_planner.homescreen.view.HomeActivity;
import com.example.food_planner.onboarding.adapter.OnboardingAdapter;
import com.example.food_planner.onboarding.model.OnboardingItem;
import com.example.food_planner.signin.view.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private MaterialButton buttonNext;
    private MaterialButton buttonGetStarted;
    private TextView buttonGuest;
    private TextView buttonSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        layoutOnboardingIndicators = findViewById(R.id.layoutIndicators);
        buttonNext = findViewById(R.id.buttonNext);
        buttonSkip = findViewById(R.id.buttonSkip);
        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        buttonGuest = findViewById(R.id.buttonGuest);

        setupOnboardingItems();

        ViewPager2 onboardingViewPager = findViewById(R.id.viewPagerOnboarding);
        onboardingViewPager.setAdapter(onboardingAdapter);

        // Apply the Custom Parallax Animation
        onboardingViewPager.setPageTransformer(new ParallaxPageTransformer());

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);

                // Smooth Animation for Button Transitions
                if (position == onboardingAdapter.getItemCount() - 1) {
                    // Last Page State
                    animateButtonSwitch(buttonNext, buttonGetStarted);

                    buttonSkip.animate().alpha(0f).setDuration(200).withEndAction(() -> buttonSkip.setVisibility(View.INVISIBLE));

                    buttonGuest.setVisibility(View.VISIBLE);
                    buttonGuest.setAlpha(0f);
                    buttonGuest.animate().alpha(1f).setDuration(300).start();
                } else {
                    // Normal Pages State
                    if (buttonGetStarted.getVisibility() == View.VISIBLE) {
                        animateButtonSwitch(buttonGetStarted, buttonNext);
                    }

                    if (buttonSkip.getVisibility() != View.VISIBLE) {
                        buttonSkip.setVisibility(View.VISIBLE);
                        buttonSkip.animate().alpha(1f).setDuration(200).start();
                    }

                    buttonGuest.animate().alpha(0f).setDuration(200).withEndAction(() -> buttonGuest.setVisibility(View.INVISIBLE));
                }
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
            }
        });

        buttonSkip.setOnClickListener(v -> navigateToLogin());
        buttonGetStarted.setOnClickListener(v -> navigateToLogin());
        buttonGuest.setOnClickListener(v -> navigateToGuest());
    }

    private void animateButtonSwitch(View viewToHide, View viewToShow) {
        viewToHide.animate().scaleX(0.8f).scaleY(0.8f).alpha(0f).setDuration(200).withEndAction(() -> {
            viewToHide.setVisibility(View.GONE);
            viewToShow.setVisibility(View.VISIBLE);
            viewToShow.setAlpha(0f);
            viewToShow.setScaleX(0.8f);
            viewToShow.setScaleY(0.8f);
            viewToShow.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(200).start();
        });
    }

    // Custom Transformer for "Parallax" effect
    public static class ParallaxPageTransformer implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            View image = page.findViewById(R.id.imageOnboarding);
            View textContainer = page.findViewById(R.id.textContainer);

            if (position < -1) { // [-Infinity,-1)
                page.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Parallax Effect: Move image slower than the text
                if (image != null) {
                    image.setTranslationX(-position * (page.getWidth() / 2)); // Image moves half speed
                }

                // Text Effect: Fade out and slide slightly
                if (textContainer != null) {
                    textContainer.setAlpha(1 - Math.abs(position));
                    textContainer.setTranslationX(position * (page.getWidth() / 4)); // Text moves quarter speed
                }

                page.setAlpha(1f);
            } else { // (1,+Infinity]
                page.setAlpha(0f);
            }
        }
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        // Screen 1: Welcome
        OnboardingItem item1 = new OnboardingItem(
                R.drawable.onboarding_1, // Change this to your actual image name
                "Welcome to Preperroni",
                "The secret ingredient to stress-free cooking. Explore meals based on ingredients, countries, and categories.");

        // Screen 2: Features (Planning)
        OnboardingItem item2 = new OnboardingItem(
                R.drawable.onboarding_2, // Change this to your actual image name
                "Plan. Prep. Favorite.",
                "Organize your weekly meals. Add recipes to your calendar and save your favorites for quick access.");

        // Screen 3: Modes (Dark/Light)
        OnboardingItem item3 = new OnboardingItem(
                R.drawable.onboarding_3, // Change this to your actual image name
                "Your Kitchen, Your Vibe",
                "Switch seamlessly between Light & Dark mode. No internet? Offline mode keeps your recipes ready.");

        // Screen 4: Guest/Start
        OnboardingItem item4 = new OnboardingItem(
                R.drawable.onboarding_4, // Change this to your actual image name
                "Ready to Cook?",
                "Create an account to sync across devices, or try Guest Mode to start browsing instantly.");

        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);
        onboardingItems.add(item4);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators() {
        if (onboardingAdapter == null) return;

        layoutOnboardingIndicators.removeAllViews();
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(12, 0, 12, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_active));
                // Optional: Animate the active dot
                imageView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).start();
            } else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive));
                imageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
            }
        }
    }

    private void navigateToLogin() {
        completeOnboarding();
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToGuest() {
        completeOnboarding();
        SharedPreferences sharedPreferences = getSharedPreferences("FoodPlannerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isGuest", true);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void completeOnboarding() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodPlannerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }
}