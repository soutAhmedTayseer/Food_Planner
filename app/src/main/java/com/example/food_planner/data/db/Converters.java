package com.example.food_planner.data.db;

import androidx.room.TypeConverter;
import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

// Room can only save primitives (int, string). This class converts Objects -> JSON Strings and back.
public class Converters {

    // writing to DB: List<Ingredient> -> String (JSON)
    @TypeConverter
    public static String fromIngredientList(List<MealDetail.Ingredient> list) {
        return new Gson().toJson(list);
    }

    // reading from DB: String (JSON) -> List<Ingredient>
    @TypeConverter
    public static List<MealDetail.Ingredient> toIngredientList(String value) {
        Type listType = new TypeToken<List<MealDetail.Ingredient>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    // writing to DB: List<String> -> String
    @TypeConverter
    public static String fromStringList(List<String> list) {
        return new Gson().toJson(list);
    }

    // reading from DB: String -> List<String>
    @TypeConverter
    public static List<String> toStringList(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
}