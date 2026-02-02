package com.example.food_planner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SharedPrefManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";
    private static final String KEY_FAVS = "favorite_meals";

    // New Keys for Daily Meal
    private static final String KEY_DAILY_MEAL = "daily_meal_obj";
    private static final String KEY_DAILY_EXPIRY = "daily_meal_expiry";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- Favorites Logic (Existing) ---
    public List<MealDetail> getFavorites() {
        String json = sharedPreferences.getString(KEY_FAVS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<MealDetail>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void addMealToFavorites(MealDetail meal) {
        List<MealDetail> currentList = getFavorites();
        // Assuming getId() is the correct getter based on your code
        for (MealDetail m : currentList) {
            if (m.getId().equals(meal.getId())) return;
        }
        currentList.add(meal);
        saveList(currentList);
    }

    public void removeMealFromFavorites(String mealId) {
        List<MealDetail> currentList = getFavorites();
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId().equals(mealId)) {
                currentList.remove(i);
                saveList(currentList);
                return;
            }
        }
    }

    public boolean isFavorite(String mealId) {
        List<MealDetail> currentList = getFavorites();
        for (MealDetail m : currentList) {
            if (m.getId().equals(mealId)) return true;
        }
        return false;
    }

    private void saveList(List<MealDetail> list) {
        String json = gson.toJson(list);
        sharedPreferences.edit().putString(KEY_FAVS, json).apply();
    }

    // --- Daily Meal Logic (New) ---

    public void saveDailyMeal(MealDetail meal) {
        // 1. Serialize the full object
        String mealJson = gson.toJson(meal);

        // 2. Calculate Expiry: 12:00 PM Tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to tomorrow
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Set 12 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long expiryTime = calendar.getTimeInMillis();

        // 3. Save
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DAILY_MEAL, mealJson);
        editor.putLong(KEY_DAILY_EXPIRY, expiryTime);
        editor.apply();
    }

    /**
     * Returns the saved meal ONLY if it hasn't expired.
     * Returns null if expired or not found.
     */
    public MealDetail getValidDailyMeal() {
        long expiryTime = sharedPreferences.getLong(KEY_DAILY_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime < expiryTime) {
            String json = sharedPreferences.getString(KEY_DAILY_MEAL, null);
            if (json != null) {
                return gson.fromJson(json, MealDetail.class);
            }
        }
        return null; // Expired or empty
    }
}