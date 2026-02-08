package com.example.food_planner.favoritescreen.presenter;

import com.example.food_planner.model.MealDetail;

public interface FavoritesPresenter {
    void getFavorites();
    void checkMode(); // Checks if user is guest or logged in
    void onGuestLoginClick();
    void onMealClick(MealDetail meal);
    void onDestroy();
}