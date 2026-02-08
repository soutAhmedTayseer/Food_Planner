package com.example.food_planner.signup.view;

import android.content.Intent;

public interface SignupView {
    // Navigation
    void navigateToHome();
    void navigateToLogin();

    // UI Feedback
    void showLoading();
    void hideLoading();
    void showSuccess(String message);
    void showError(String message);

    // Google Sign In Trigger
    void launchGoogleSignIn(Intent signInIntent);
}