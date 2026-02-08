package com.example.food_planner.data.datasource.remote;

import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealResponse;
import io.reactivex.rxjava3.core.Single;

public interface RemoteDataSource {
    Single<Meal> getCategories();
    Single<Meal> getAreas();
    Single<Meal> getIngredients();
    Single<MealResponse> getRandomMeal();
    Single<MealResponse> searchMealByName(String mealName);
    Single<MealResponse> getMealById(String mealId);
    Single<Meal> filterByCategory(String category);
    Single<Meal> filterByArea(String area);
    Single<Meal> filterByIngredient(String ingredient);
}