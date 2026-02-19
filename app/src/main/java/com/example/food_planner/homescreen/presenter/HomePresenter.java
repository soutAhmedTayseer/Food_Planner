package com.example.food_planner.homescreen.presenter;

import com.example.food_planner.model.MealDetail;

public interface HomePresenter {
    void getDailyMeal(); // Check cache/preferences for daily meal

    void requestNewDailyMeal(); // Fetch fresh daily meal from network (flip card)

    void getInspirationMeals(boolean forceRefresh); // Load carousel data

    void onInspirationMealClicked(MealDetail meal); // Handle click on carousel item

    void onDestroy(); // Cleanup resources
}