package com.example.food_planner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";
    private static final String KEY_FAVS = "favorite_meals";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<MealDetail> getFavorites() {
        String json = sharedPreferences.getString(KEY_FAVS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<MealDetail>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addMealToFavorites(MealDetail meal) {
        List<MealDetail> currentList = getFavorites();
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
}