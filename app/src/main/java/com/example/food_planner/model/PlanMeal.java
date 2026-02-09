package com.example.food_planner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;


import com.example.food_planner.data.db.Converters;

import java.util.ArrayList;
import java.util.List;

// Composite Key: mealId + planDate + userId
// This allows:
// 1. Different users to plan the same meal.
// 2. The SAME user to plan the SAME meal on DIFFERENT days.
// 3. But PREVENTS the same user from planning the same meal twice on the SAME day.
@Entity(tableName = "plan_meals", primaryKeys = {"mealId", "planDate", "userId"})
public class PlanMeal {

    @NonNull
    private String mealId;

    @NonNull
    private String planDate;

    @NonNull
    private String userId;

    private String mealName;
    private String mealThumb;
    private String mealArea;
    private String mealCategory;
    private String mealInstructions;
    private String mealYoutube;

    // Uses TypeConverters to save the list of ingredients as a String
    @TypeConverters(Converters.class)
    private List<MealDetail.Ingredient> ingredients = new ArrayList<>();

    public PlanMeal(@NonNull String mealId, @NonNull String planDate, @NonNull String userId, String mealName, String mealThumb, String mealArea, String mealCategory, String mealInstructions, String mealYoutube, List<MealDetail.Ingredient> ingredients) {
        this.mealId = mealId;
        this.planDate = planDate;
        this.userId = userId;
        this.mealName = mealName;
        this.mealThumb = mealThumb;
        this.mealArea = mealArea;
        this.mealCategory = mealCategory;
        this.mealInstructions = mealInstructions;
        this.mealYoutube = mealYoutube;
        this.ingredients = ingredients;
    }

    // --- CONVERSION HELPER ---
    // Factory Method: Creates a PlanMeal from a MealDetail + Date + UserID
    public static PlanMeal fromMealDetail(MealDetail meal, String date, String userId) {
        return new PlanMeal(meal.getId(), date, userId, meal.getName(), meal.getThumbUrl(), meal.getArea(), meal.getCategory(), meal.getInstructions(), meal.getYoutubeUrl(), meal.getIngredients());
    }

    // Helper: Converts this back to a MealDetail so we can navigate to the Details Screen.
    public MealDetail toMealDetail() {
        MealDetail meal = new MealDetail();
        meal.setId(this.mealId);
        meal.setUserId(this.userId);
        meal.setName(this.mealName);
        meal.setThumbUrl(this.mealThumb);
        meal.setArea(this.mealArea);
        meal.setCategory(this.mealCategory);
        meal.setInstructions(this.mealInstructions);
        meal.setYoutubeUrl(this.mealYoutube);
        meal.setIngredients(this.ingredients);
        return meal;
    }

    @NonNull
    public String getMealId() {
        return mealId;
    }

    public void setMealId(@NonNull String mealId) {
        this.mealId = mealId;
    }

    @NonNull
    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(@NonNull String planDate) {
        this.planDate = planDate;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getMealName() {
        return mealName;
    }

    public String getMealThumb() {
        return mealThumb;
    }

    public String getMealArea() {
        return mealArea;
    }

    public String getMealCategory() {
        return mealCategory;
    }

    public String getMealInstructions() {
        return mealInstructions;
    }

    public String getMealYoutube() {
        return mealYoutube;
    }

    public List<MealDetail.Ingredient> getIngredients() {
        return ingredients;
    }
}