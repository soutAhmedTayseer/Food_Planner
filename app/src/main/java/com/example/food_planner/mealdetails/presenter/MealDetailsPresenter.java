package com.example.food_planner.mealdetails.presenter;

import com.example.food_planner.model.MealDetail;

public interface MealDetailsPresenter {
    void setMealData(MealDetail mealDetail);
    void addToPlan(String date);
    void checkFavoriteStatus();
    void onFavoriteClick();
    void removeFromFavoritesConfirmed();
    void onDestroy();
}