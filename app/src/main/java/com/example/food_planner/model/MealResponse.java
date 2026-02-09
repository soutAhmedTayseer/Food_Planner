package com.example.food_planner.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class MealResponse {
    // API returns "meals" list of full details.
    @SerializedName("meals")
    private List<MealDetail> meals;

    public List<MealDetail> getMeals() {
        if (meals == null) return new ArrayList<>();
        return meals;
    }

    public void setMeals(List<MealDetail> meals) {
        this.meals = meals;
    }
}