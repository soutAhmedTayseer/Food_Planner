package com.example.food_planner.data.datasource.local;

import androidx.room.*;
import com.example.food_planner.model.PlanMeal;
import java.util.List;
import io.reactivex.rxjava3.core.*;

@Dao
public interface PlanDao {
    // Insert a planned meal. If it conflicts, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPlan(PlanMeal planMeal);

    @Delete
    Completable deletePlan(PlanMeal planMeal);

    // Get meals for a specific DATE and USER.
    @Query("SELECT * FROM plan_meals WHERE planDate = :date AND userId = :userId")
    Flowable<List<PlanMeal>> getPlansByDate(String date, String userId);

    // Get every single planned meal for the user.
    @Query("SELECT * FROM plan_meals WHERE userId = :userId")
    Flowable<List<PlanMeal>> getAllPlans(String userId);
}