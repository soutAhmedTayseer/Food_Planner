package com.example.food_planner.data.datasource.remote;

import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealResponse;
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

    @GET("random.php")
    Single<MealResponse> getRandomMeal();

    @GET("search.php")
    Single<MealResponse> searchMealByName(@Query("s") String mealName);

    @GET("lookup.php")
    Single<MealResponse> getMealById(@Query("i") String mealId);

    @GET("filter.php")
    Single<Meal> filterByCategory(@Query("c") String category);

    @GET("filter.php")
    Single<Meal> filterByArea(@Query("a") String area);

    @GET("filter.php")
    Single<Meal> filterByIngredient(@Query("i") String ingredient);
}