package com.example.food_planner.repository;

import android.content.Context;

import com.example.food_planner.db.FoodPlannerDatabase;
import com.example.food_planner.db.MealDao;
import com.example.food_planner.db.PlanDao;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.utils.SharedPrefManager;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepository {

    private final MealDao mealDao;
    private final PlanDao planDao;     // <--- Add this
    private final SharedPrefManager sharedPrefManager;

    public MealRepository(Context context) {
        FoodPlannerDatabase db = FoodPlannerDatabase.getInstance(context);
        mealDao = db.mealDao();
        planDao = db.planDao();        // <--- Initialize this
        sharedPrefManager = new SharedPrefManager(context);
    }

    // Helper: Always get the current logged-in user ID
    private String getCurrentUserId() {
        String uid = sharedPrefManager.getUserUid();
        return uid.isEmpty() ? "guest" : uid;
    }

    // --- FAVORITES ---
    public Completable addToFavorites(MealDetail meal) {
        meal.setUserId(getCurrentUserId());
        return mealDao.insertFav(meal).subscribeOn(Schedulers.io());
    }

    public Completable removeFromFavorites(MealDetail meal) {
        meal.setUserId(getCurrentUserId());
        return mealDao.deleteFav(meal).subscribeOn(Schedulers.io());
    }

    public Flowable<List<MealDetail>> getStoredMeals() {
        return mealDao.getFavMeals(getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isFavorite(String mealId) {
        return mealDao.isFav(mealId, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    // --- CALENDAR / PLAN (ADD THESE METHODS) ---

    public Completable addToPlan(MealDetail meal, String date) {
        // Convert MealDetail -> PlanMeal with the specific date
        PlanMeal planMeal = PlanMeal.fromMealDetail(meal, date, getCurrentUserId());
        return planDao.insertPlan(planMeal).subscribeOn(Schedulers.io());
    }

    public Completable removeFromPlan(PlanMeal planMeal) {
        return planDao.deletePlan(planMeal).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getPlansByDate(String date) {
        return planDao.getPlansByDate(date, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getAllPlans() {
        return planDao.getAllPlans(getCurrentUserId()).subscribeOn(Schedulers.io());
    }
}