package com.example.food_planner.network;

import com.example.food_planner.model.Meal;
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
}