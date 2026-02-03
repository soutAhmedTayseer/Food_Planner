package com.example.food_planner.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.food_planner.model.UserEntity;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class FoodPlannerDatabase extends RoomDatabase {
    private static volatile FoodPlannerDatabase INSTANCE;

    public abstract UserDao userDao();

    public static FoodPlannerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FoodPlannerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FoodPlannerDatabase.class, "food_planner_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}