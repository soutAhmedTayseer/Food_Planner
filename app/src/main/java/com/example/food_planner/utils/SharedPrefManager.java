package com.example.food_planner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;

import java.util.Calendar;

public class SharedPrefManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";

    // Keys
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_GUEST = "is_guest";

    // Prefixes for dynamic keys (e.g. "daily_meal_USER123")
    private static final String KEY_DAILY_MEAL_PREFIX = "daily_meal_";
    private static final String KEY_DAILY_EXPIRY_PREFIX = "daily_meal_expiry_";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

// --- SESSION MANAGEMENT ---

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public boolean isGuest() {
        return sharedPreferences.getBoolean(KEY_IS_GUEST, false);
    }
    public void saveUserSession(String email, String uid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }
    public void saveGuestSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, true);
        editor.putString(KEY_USER_UID, "guest_id");
        editor.apply();
    }

    public String getUserUid() {
        return sharedPreferences.getString(KEY_USER_UID, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_IS_GUEST);
        editor.remove(KEY_USER_UID);
        editor.remove(KEY_USER_EMAIL);
        // Do NOT clear everything (editor.clear()) if you want to keep the Daily Meal cache
        editor.apply();
    }

    // --- DAILY MEAL (USER ISOLATED) ---

    public void saveDailyMeal(MealDetail meal) {
        String uid = getUserUid();
        if (uid.isEmpty()) return; // Don't save if no user

        String mealJson = gson.toJson(meal);

        // Calculate Midnight (00:00) of Tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Tomorrow
        calendar.set(Calendar.HOUR_OF_DAY, 0); // 00:00 (Midnight)
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long expiryTime = calendar.getTimeInMillis();

        // Save with specific User Key
        sharedPreferences.edit()
                .putString(KEY_DAILY_MEAL_PREFIX + uid, mealJson)
                .putLong(KEY_DAILY_EXPIRY_PREFIX + uid, expiryTime)
                .apply();
    }

    public MealDetail getValidDailyMeal() {
        String uid = getUserUid();
        if (uid.isEmpty()) return null;

        long expiryTime = sharedPreferences.getLong(KEY_DAILY_EXPIRY_PREFIX + uid, 0);
        long currentTime = System.currentTimeMillis();

        // If current time is BEFORE expiry, the meal is valid
        if (currentTime < expiryTime) {
            String json = sharedPreferences.getString(KEY_DAILY_MEAL_PREFIX + uid, null);
            if (json != null) {
                return gson.fromJson(json, MealDetail.class);
            }
        }
        return null; // Expired or Empty
    }
}