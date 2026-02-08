package com.example.food_planner.homescreen.view;

import com.example.food_planner.model.MealDetail;
import java.util.List;

public interface HomeView {
    void showLoading();
    void hideLoading();
    void showDailyMeal(MealDetail meal);
    void showMysteryCard();
    void animateFlipToMeal(MealDetail meal);
    void showInspirationMeals(List<MealDetail> meals);
    void showError(String message);
    void showNetworkError();
}