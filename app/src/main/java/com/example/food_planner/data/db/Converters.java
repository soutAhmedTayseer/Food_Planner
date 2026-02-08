package com.example.food_planner.data.db;

import androidx.room.TypeConverter;
import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromIngredientList(List<MealDetail.Ingredient> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<MealDetail.Ingredient> toIngredientList(String value) {
        Type listType = new TypeToken<List<MealDetail.Ingredient>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
}