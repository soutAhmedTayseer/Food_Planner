package com.example.food_planner.data.datasource.remote;

import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealResponse;
import io.reactivex.rxjava3.core.Single;

public class RemoteDataSourceImpl implements RemoteDataSource {
    private static RemoteDataSourceImpl instance;
    private final FoodApi foodApi;

    public static RemoteDataSourceImpl getInstance() {
        if (instance == null) {
            instance = new RemoteDataSourceImpl();
        }
        return instance;
    }

    private RemoteDataSourceImpl() {
        // Assuming NetworkClient is updated to return Retrofit or FoodApi directly
        // If NetworkClient is in com.example.food_planner.network, import it.
        // If moved to remote package, usage is direct.
        this.foodApi = com.example.food_planner.network.NetworkClient.getRetrofitInstance().create(FoodApi.class);
    }

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