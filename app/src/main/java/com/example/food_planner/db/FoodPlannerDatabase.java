package com.example.food_planner.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.model.UserEntity;

@Database(entities = {UserEntity.class, MealDetail.class, PlanMeal.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class FoodPlannerDatabase extends RoomDatabase {
    private static volatile FoodPlannerDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract MealDao mealDao();

    public abstract PlanDao planDao();

    public static FoodPlannerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FoodPlannerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FoodPlannerDatabase.class, "food_planner_db")
                            .fallbackToDestructiveMigration() // Handles schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}