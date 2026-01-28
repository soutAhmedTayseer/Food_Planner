package com.example.food_planner.network;

import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealResponse; // Make sure you have this
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FoodApi {
    @GET("list.php?c=list")
    Single<Meal> getCategories();

    @GET("list.php?a=list")
    Single<Meal> getAreas();

    @GET("list.php?i=list")
    Single<Meal> getIngredients();

    // --- UPDATED: Return MealResponse (Full Details) instead of Meal ---
    @GET("random.php")
    Single<MealResponse> getRandomMeal();

    // ... other search endpoints
    @GET("search.php")
    Single<MealResponse> searchMealByName(@Query("s") String mealName);

    @GET("lookup.php")
    Single<MealResponse> getMealById(@Query("i") String mealId);
}