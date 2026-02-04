package com.example.food_planner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.example.food_planner.db.Converters;

import java.util.ArrayList;
import java.util.List;

// Primary Key: Combining mealId + planDate + userId ensures you can have
// the same meal on different days, but not duplicates on the same day.
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

    // Use Converter for Ingredients
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
    // Converts a regular MealDetail to a PlanMeal for a specific date
    public static PlanMeal fromMealDetail(MealDetail meal, String date, String userId) {
        return new PlanMeal(meal.getId(), date, userId, meal.getName(), meal.getThumbUrl(), meal.getArea(), meal.getCategory(), meal.getInstructions(), meal.getYoutubeUrl(), meal.getIngredients());
    }

    // Converts back to MealDetail for navigation
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