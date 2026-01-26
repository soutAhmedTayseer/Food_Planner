package com.example.food_planner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.homescreen.HomeActivity;
import com.example.food_planner.signin.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this XML has your Lottie Animation

        // Simulate Splash Screen delay (e.g., 3 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserStatus();
        }, 3000);
    }

    private void checkUserStatus() {
        // TODO: Later, replace "PREFS" with your actual SharedPreference Helper class
        SharedPreferences prefs = getSharedPreferences("FoodPlannerPrefs", MODE_PRIVATE);
        boolean isGuest = prefs.getBoolean("isGuest", false);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn || isGuest) {
            // User is known, go to Home
            intent = new Intent(MainActivity.this, HomeActivity.class);
        } else {
            // New user, go to Login
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Remove Splash from back stack
    }
}