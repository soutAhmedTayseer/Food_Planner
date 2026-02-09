package com.example.food_planner.data.datasource.remote;

import com.example.food_planner.model.*;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Interface for Retrofit Defines how we talk to the server.
public interface FoodApi {
    // Get list of meal categories
    @GET("list.php?c=list")
    Single<Meal> getCategories();

    // Get list of areas/countries
    @GET("list.php?a=list")
    Single<Meal> getAreas();

    // Get list of ingredients
    @GET("list.php?i=list")
    Single<Meal> getIngredients();

    // Get a single random meal
    @GET("random.php")
    Single<MealResponse> getRandomMeal();

    // Search by name
    @GET("search.php")
    Single<MealResponse> searchMealByName(@Query("s") String mealName);

    // Get full details by ID
    @GET("lookup.php")
    Single<MealResponse> getMealById(@Query("i") String mealId);

    // --- Filters ---
    @GET("filter.php")
    Single<Meal> filterByCategory(@Query("c") String category);

    @GET("filter.php")
    Single<Meal> filterByArea(@Query("a") String area);

    @GET("filter.php")
    Single<Meal> filterByIngredient(@Query("i") String ingredient);
}