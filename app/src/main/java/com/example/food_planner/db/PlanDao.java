package com.example.food_planner.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.food_planner.model.PlanMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPlan(PlanMeal planMeal);

    @Delete
    Completable deletePlan(PlanMeal planMeal);

    // Get meals for a specific date (User Isolated)
    @Query("SELECT * FROM plan_meals WHERE planDate = :date AND userId = :userId")
    Flowable<List<PlanMeal>> getPlansByDate(String date, String userId);

    // Get all plans (For the calendar indicators)
    @Query("SELECT * FROM plan_meals WHERE userId = :userId")
    Flowable<List<PlanMeal>> getAllPlans(String userId);
}