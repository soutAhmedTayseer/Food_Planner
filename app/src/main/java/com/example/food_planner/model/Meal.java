package com.example.food_planner.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Meal {
    // The API sends: { "meals": [ ... ] }
    // We map that "meals" key to this list.
    @SerializedName("meals")
    private List<MealItem> items;

    // Safety: Never return null, return an empty list instead to prevent app crashes.
    public List<MealItem> getItems() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }
}