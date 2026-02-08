package com.example.food_planner.mealslist.presenter;

public interface MealsListPresenter {
    void getMeals(String type, String queryName);
    void searchLocalList(String query);
    void getMealDetails(String mealId);
    void onDestroy();
}