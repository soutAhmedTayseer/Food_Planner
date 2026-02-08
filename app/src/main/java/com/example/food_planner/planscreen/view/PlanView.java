package com.example.food_planner.planscreen.view;

import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.model.MealDetail; // Needed for navigation mapping
import java.util.List;

public interface PlanView {
    void showGuestMode();
    void hideGuestMode();
    void showLoginDialog();
    void navigateToLogin();

    void showPlanMeals(List<PlanMeal> meals);
    void showEmptyState();
    void updateDateHeader(String dateText);

    void showDeleteConfirmation(PlanMeal meal);
    void showMessage(String message);
    void showError(String error);

    void navigateToDetails(MealDetail mealDetail);
}