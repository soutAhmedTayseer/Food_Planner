package com.example.food_planner.data.datasource.local;

import android.content.Context;
import com.example.food_planner.data.db.FoodPlannerDatabase;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.model.UserEntity;
import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class LocalDataSourceImpl implements LocalDataSource {

    private final MealDao mealDao;
    private final PlanDao planDao;
    private final UserDao userDao;
    private static LocalDataSourceImpl instance;

    public static LocalDataSourceImpl getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDataSourceImpl(context);
        }
        return instance;
    }

    private LocalDataSourceImpl(Context context) {
        FoodPlannerDatabase db = FoodPlannerDatabase.getInstance(context);
        this.mealDao = db.mealDao();
        this.planDao = db.planDao();
        this.userDao = db.userDao();
    }

    // --- Favorites ---
    @Override
    public Completable insertFav(MealDetail meal) {
        return mealDao.insertFav(meal);
    }

    @Override
    public Completable deleteFav(MealDetail meal) {
        return mealDao.deleteFav(meal);
    }

    @Override
    public Flowable<List<MealDetail>> getFavMeals(String userId) {
        return mealDao.getFavMeals(userId);
    }

    @Override
    public Single<Boolean> isFav(String mealId, String userId) {
        return mealDao.isFav(mealId, userId);
    }

    // --- Planning ---
    @Override
    public Completable insertPlan(PlanMeal planMeal) {
        return planDao.insertPlan(planMeal);
    }

    @Override
    public Completable deletePlan(PlanMeal planMeal) {
        return planDao.deletePlan(planMeal);
    }

    @Override
    public Flowable<List<PlanMeal>> getPlansByDate(String date, String userId) {
        return planDao.getPlansByDate(date, userId);
    }

    @Override
    public Flowable<List<PlanMeal>> getAllPlans(String userId) {
        return planDao.getAllPlans(userId);
    }

    // --- User ---
    @Override
    public Completable insertUser(UserEntity user) {
        return userDao.insertUser(user);
    }

    @Override
    public UserEntity getUser() {
        return userDao.getUser();
    }

    @Override
    public Completable clearUser() {
        return userDao.clearUser();
    }
}