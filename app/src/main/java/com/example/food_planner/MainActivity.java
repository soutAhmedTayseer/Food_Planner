package com.example.food_planner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.homescreen.view.HomeActivity;
import com.example.food_planner.signin.view.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this XML has your Lottie Animation

        // Simulate Splash Screen delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserStatus();
        }, 3000);
    }

    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences("FoodPlannerPrefs", MODE_PRIVATE);
        boolean isGuest = prefs.getBoolean("isGuest", false);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isFirstTime()) {
            intent = new Intent(MainActivity.this, com.example.food_planner.onboarding.view.OnboardingActivity.class);
        } else if (isLoggedIn || isGuest) {
            // User is known, go to Home
            intent = new Intent(MainActivity.this, HomeActivity.class);
        } else {
            // New user, go to Login
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Remove Splash from back stack
    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences("FoodPlannerPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isFirstTime", true);
    }
}