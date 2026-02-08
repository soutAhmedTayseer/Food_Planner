package com.example.food_planner.mealslist.view;

import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.MealItem;
import java.util.List;

public interface MealsListView {
    void showLoading(boolean isLoading);
    void showMeals(List<MealItem> meals);
    void showError(String message);
    void navigateToDetails(MealDetail meal);
}