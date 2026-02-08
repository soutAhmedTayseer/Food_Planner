package com.example.food_planner.data.datasource.local;

import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.model.UserEntity;
import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface LocalDataSource {
    // Favorites
    Completable insertFav(MealDetail meal);
    Completable deleteFav(MealDetail meal);
    Flowable<List<MealDetail>> getFavMeals(String userId);
    Single<Boolean> isFav(String mealId, String userId);

    // Planning
    Completable insertPlan(PlanMeal planMeal);
    Completable deletePlan(PlanMeal planMeal);
    Flowable<List<PlanMeal>> getPlansByDate(String date, String userId);
    Flowable<List<PlanMeal>> getAllPlans(String userId);

    // User
    Completable insertUser(UserEntity user);
    UserEntity getUser();
    Completable clearUser();
}