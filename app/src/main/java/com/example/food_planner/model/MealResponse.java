package com.example.food_planner.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class MealResponse {

    // The API always returns a key called "meals" containing a list of objects
    @SerializedName("meals")
    private List<MealDetail> meals;

    // Getter with safety check
    public List<MealDetail> getMeals() {
        if (meals == null) {
            return new ArrayList<>();
        }
        return meals;
    }

    public void setMeals(List<MealDetail> meals) {
        this.meals = meals;
    }
}