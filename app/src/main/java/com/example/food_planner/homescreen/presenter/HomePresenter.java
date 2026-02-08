package com.example.food_planner.homescreen.presenter;

public interface HomePresenter {
    void getDailyMeal();           // Check cache/preferences for daily meal
    void requestNewDailyMeal();    // Fetch fresh daily meal from network (flip card)
    void getInspirationMeals();    // Load carousel data
    void onDestroy();              // Cleanup resources
}