package com.example.food_planner.signin.view;

import android.content.Intent;

public interface LoginView {
    void showLoading();
    void hideLoading();
    void showSuccess(String message);
    void showError(String message);
    void navigateToHome();
    void navigateToSignUp();
    void launchGoogleSignIn(Intent signInIntent);

    boolean isNetworkAvailable();
}