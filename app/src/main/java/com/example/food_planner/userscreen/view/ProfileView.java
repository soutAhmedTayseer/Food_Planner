package com.example.food_planner.userscreen.view;

public interface ProfileView {
    // UI Updates
    void showGuestMode();
    void showUserMode(String name, String email);

    // Navigation
    void navigateToLogin();

    // Feedback
    void showSuccess(String message);
    void showError(String message);

}