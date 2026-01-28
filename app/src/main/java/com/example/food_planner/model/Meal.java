package com.example.food_planner.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Meal {
    // All list.php endpoints (Categories, Areas, Ingredients) return "meals"
    @SerializedName("meals")
    private List<MealItem> items;

    // Helper to always return a list, even if empty (Prevents Crashes)
    public List<MealItem> getItems() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }
}