package com.example.food_planner.userscreen.presenter;

public interface ProfilePresenter {
    void loadUserProfile();
    void updateName(String newName);
    void updatePassword(String currentPassword, String newPassword);
    void deleteAccount(String password);
    void logout();
    void onDestroy();
}