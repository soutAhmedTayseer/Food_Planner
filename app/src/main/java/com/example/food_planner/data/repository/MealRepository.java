package com.example.food_planner.data.repository;

import android.content.Context;

import com.example.food_planner.data.datasource.local.LocalDataSource;
import com.example.food_planner.data.datasource.local.LocalDataSourceImpl;
import com.example.food_planner.data.datasource.remote.RemoteDataSource;
import com.example.food_planner.data.datasource.remote.RemoteDataSourceImpl;
import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.MealResponse;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.utils.SharedPrefManager;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SharedPrefManager sharedPrefManager;

    private static MealRepository instance;

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

    private String getCurrentUserId() {
        String uid = sharedPrefManager.getUserUid();
        return uid.isEmpty() ? "guest" : uid;
    }

    // --- DAILY MEAL (HOME SCREEN SUPPORT) ---
    public MealDetail getValidDailyMeal() {
        return sharedPrefManager.getValidDailyMeal();
    }

    public void saveDailyMeal(MealDetail meal) {
        sharedPrefManager.saveDailyMeal(meal);
    }

    // --- REMOTE CALLS ---
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

    // --- FAVORITES ---
    public Completable addToFavorites(MealDetail meal) {
        meal.setUserId(getCurrentUserId());
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

    // --- PLAN ---
    public Completable addToPlan(MealDetail meal, String date) {
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