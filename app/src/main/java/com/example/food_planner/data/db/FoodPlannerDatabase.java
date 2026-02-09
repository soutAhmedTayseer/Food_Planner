package com.example.food_planner.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.food_planner.data.datasource.local.*;
import com.example.food_planner.model.*;

// @Database: Defines our schema (User, MealDetail, PlanMeal) and version.
@Database(entities = {UserEntity.class, MealDetail.class, PlanMeal.class}, version = 4, exportSchema = false)
// @TypeConverters: Room doesn't know how to save Lists/Objects. We tell it to use 'Converters' class to turn them into Strings.
@TypeConverters({Converters.class})
public abstract class FoodPlannerDatabase extends RoomDatabase {

    // Volatile: Ensures changes to this variable are immediately visible to all other threads.
    private static volatile FoodPlannerDatabase INSTANCE;

    // DAOs: We declare these abstract methods so Room can generate the code to access our tables.
    public abstract UserDao userDao();
    public abstract MealDao mealDao();
    public abstract PlanDao planDao();

    // Singleton Pattern: Ensures we only have ONE database connection open at a time to save memory.
    public static FoodPlannerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FoodPlannerDatabase.class) { // Lock to prevent multiple threads creating it at once
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FoodPlannerDatabase.class, "food_planner_db")
                            // fallbackToDestructiveMigration: Wipes the database if we change the version number
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}