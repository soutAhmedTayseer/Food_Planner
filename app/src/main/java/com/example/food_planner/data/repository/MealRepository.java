package com.example.food_planner.data.repository;

import android.content.Context;
import com.example.food_planner.data.datasource.local.*;
import com.example.food_planner.data.datasource.remote.*;
import com.example.food_planner.model.*;
import com.example.food_planner.utils.SharedPrefManager;
import java.util.List;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepository {

    // Dependencies: We need Local DB, Remote API, and SharedPrefs.
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SharedPrefManager sharedPrefManager;

    private static MealRepository instance;

    // Singleton Pattern: Ensures the entire app uses the same Repository instance.
    public static MealRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MealRepository(context);
        }
        return instance;
    }

    private MealRepository(Context context) {
        this.localDataSource = LocalDataSourceImpl.getInstance(context);
        this.remoteDataSource = RemoteDataSourceImpl.getInstance();
        this.sharedPrefManager = new SharedPrefManager(context);
    }

    // Helper: Gets the current user ID. If no one is logged in, use "guest".
    // This ensures we save data to the correct profile.
    private String getCurrentUserId() {
        String uid = sharedPrefManager.getUserUid();
        return uid.isEmpty() ? "guest" : uid;
    }

    // --- DAILY MEAL (Cached in SharedPrefs) ---
    // We don't want to hit the API every time we open the home screen, so we cache it.
    public MealDetail getValidDailyMeal() {
        return sharedPrefManager.getValidDailyMeal();
    }

    public void saveDailyMeal(MealDetail meal) {
        sharedPrefManager.saveDailyMeal(meal);
    }

    // --- REMOTE CALLS (API) ---
    // These methods just pass the call to the RemoteDataSource.
    public Single<MealResponse> getRandomMeal() {
        return remoteDataSource.getRandomMeal();
    }

    public Single<Meal> getCategories() {
        return remoteDataSource.getCategories();
    }

    public Single<Meal> getAreas() {
        return remoteDataSource.getAreas();
    }

    public Single<Meal> getIngredients() {
        return remoteDataSource.getIngredients();
    }

    // --- FILTERING & SEARCH ---
    public Single<Meal> filterByCategory(String category) {
        return remoteDataSource.filterByCategory(category);
    }

    public Single<Meal> filterByArea(String area) {
        return remoteDataSource.filterByArea(area);
    }

    public Single<Meal> filterByIngredient(String ingredient) {
        return remoteDataSource.filterByIngredient(ingredient);
    }

    public Single<MealResponse> getMealById(String mealId) {
        return remoteDataSource.getMealById(mealId);
    }

    // --- FAVORITES (Local Database) ---
    // Note: We always inject the Current User ID before saving to ensure data privacy.
    public Completable addToFavorites(MealDetail meal) {
        meal.setUserId(getCurrentUserId());
        // subscribeOn(Schedulers.io()): Crucial! Moves this heavy work to a background thread.
        return localDataSource.insertFav(meal).subscribeOn(Schedulers.io());
    }

    public Completable removeFromFavorites(MealDetail meal) {
        meal.setUserId(getCurrentUserId());
        return localDataSource.deleteFav(meal).subscribeOn(Schedulers.io());
    }

    public Flowable<List<MealDetail>> getStoredMeals() {
        return localDataSource.getFavMeals(getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isFavorite(String mealId) {
        return localDataSource.isFav(mealId, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    // --- MEAL PLAN (Local Database) ---
    public Completable addToPlan(MealDetail meal, String date) {
        // We convert the generic "MealDetail" into a specific "PlanMeal" linked to a date.
        PlanMeal planMeal = PlanMeal.fromMealDetail(meal, date, getCurrentUserId());
        return localDataSource.insertPlan(planMeal).subscribeOn(Schedulers.io());
    }

    public Completable removeFromPlan(PlanMeal planMeal) {
        return localDataSource.deletePlan(planMeal).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getPlansByDate(String date) {
        return localDataSource.getPlansByDate(date, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getAllPlans() {
        return localDataSource.getAllPlans(getCurrentUserId()).subscribeOn(Schedulers.io());
    }
}