package com.example.food_planner.planscreen.presenter;

import com.example.food_planner.model.PlanMeal;

public interface PlanPresenter {
    void checkMode();
    void selectDate(int year, int month, int dayOfMonth);
    void getMealsForDate(String date);
    void onMealClick(PlanMeal meal);
    void onDeleteClick(PlanMeal meal);
    void deletePlanConfirmed(PlanMeal meal);
    void onGuestLoginClick();
    void onDestroy();
}