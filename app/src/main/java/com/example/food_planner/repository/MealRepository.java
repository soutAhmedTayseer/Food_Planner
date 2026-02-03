package com.example.food_planner.repository;

import android.content.Context;
import com.example.food_planner.db.FoodPlannerDatabase;
import com.example.food_planner.db.MealDao;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.SharedPrefManager;
import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepository {

    private final MealDao mealDao;
    private final SharedPrefManager sharedPrefManager;

    public MealRepository(Context context) {
        FoodPlannerDatabase db = FoodPlannerDatabase.getInstance(context);
        mealDao = db.mealDao();
        sharedPrefManager = new SharedPrefManager(context);
    }

    // Helper: Always get the current logged-in user ID
    private String getCurrentUserId() {
        String uid = sharedPrefManager.getUserUid();
        return uid.isEmpty() ? "guest" : uid;
    }

    public Completable addToFavorites(MealDetail meal) {
        // IMPORTANT: Attach the User ID to the meal before saving
        meal.setUserId(getCurrentUserId());
        return mealDao.insertFav(meal)
                .subscribeOn(Schedulers.io());
    }

    public Completable removeFromFavorites(MealDetail meal) {
        // Ensure we are deleting the meal belonging to THIS user
        meal.setUserId(getCurrentUserId());
        return mealDao.deleteFav(meal)
                .subscribeOn(Schedulers.io());
    }

    public Flowable<List<MealDetail>> getStoredMeals() {
        // Fetch only meals for the current user
        return mealDao.getFavMeals(getCurrentUserId())
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isFavorite(String mealId) {
        // Check if this specific user favorited this meal
        return mealDao.isFav(mealId, getCurrentUserId())
                .subscribeOn(Schedulers.io());
    }
}