package com.example.food_planner.data.datasource.local;

import com.example.food_planner.model.*;
import java.util.List;
import io.reactivex.rxjava3.core.*;

public interface LocalDataSource {
    // --- Favorites ---
    // Completable: "Just do the job and tell me when you're done (or if you failed)." No data returned.
    Completable insertFav(MealDetail meal);
    Completable deleteFav(MealDetail meal);

    // Flowable: "Keep the channel open." If the database changes, emit the new list automatically.
    Flowable<List<MealDetail>> getFavMeals(String userId);

    // Single: "Give me one result (true/false) and then close."
    Single<Boolean> isFav(String mealId, String userId);

    // --- Planning ---
    Completable insertPlan(PlanMeal planMeal);
    Completable deletePlan(PlanMeal planMeal);
    Flowable<List<PlanMeal>> getPlansByDate(String date, String userId);
    Flowable<List<PlanMeal>> getAllPlans(String userId);

    // --- User ---
    Completable insertUser(UserEntity user);
    UserEntity getUser();
    Completable clearUser();
}