package com.example.food_planner.data.datasource.remote;

import com.example.food_planner.model.*;
import io.reactivex.rxjava3.core.Single;

public class RemoteDataSourceImpl implements RemoteDataSource {
    private static RemoteDataSourceImpl instance;
    private final FoodApi foodApi;

    // Singleton: We only want one instance handling network calls.
    public static RemoteDataSourceImpl getInstance() {
        if (instance == null) {
            instance = new RemoteDataSourceImpl();
        }
        return instance;
    }

    // Constructor: Creates the Retrofit service.
    private RemoteDataSourceImpl() {
        // We get the Retrofit client from our NetworkClient helper and create the FoodApi implementation.
        this.foodApi = com.example.food_planner.network.NetworkClient.getRetrofitInstance().create(FoodApi.class);
    }

    // The following methods just forward the call to the Retrofit API interface.

    @Override
    public Single<Meal> getCategories() {
        return foodApi.getCategories();
    }

    @Override
    public Single<Meal> getAreas() {
        return foodApi.getAreas();
    }

    @Override
    public Single<Meal> getIngredients() {
        return foodApi.getIngredients();
    }

    @Override
    public Single<MealResponse> getRandomMeal() {
        return foodApi.getRandomMeal();
    }

    @Override
    public Single<MealResponse> searchMealByName(String mealName) {
        return foodApi.searchMealByName(mealName);
    }

    @Override
    public Single<MealResponse> getMealById(String mealId) {
        return foodApi.getMealById(mealId);
    }

    @Override
    public Single<Meal> filterByCategory(String category) {
        return foodApi.filterByCategory(category);
    }

    @Override
    public Single<Meal> filterByArea(String area) {
        return foodApi.filterByArea(area);
    }

    @Override
    public Single<Meal> filterByIngredient(String ingredient) {
        return foodApi.filterByIngredient(ingredient);
    }
}