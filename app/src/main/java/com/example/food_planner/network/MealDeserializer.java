package com.example.food_planner.network;

import com.example.food_planner.model.MealDetail;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class MealDeserializer implements JsonDeserializer<MealDetail> {
    @Override
    public MealDetail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        MealDetail meal = new MealDetail();

        // 1. Manually map the standard fields
        if (jsonObject.has("idMeal")) meal.setId(jsonObject.get("idMeal").getAsString());
        if (jsonObject.has("strMeal")) meal.setName(jsonObject.get("strMeal").getAsString());
        if (jsonObject.has("strCategory")) meal.setCategory(jsonObject.get("strCategory").getAsString());
        if (jsonObject.has("strArea")) meal.setArea(jsonObject.get("strArea").getAsString());
        if (jsonObject.has("strInstructions")) meal.setInstructions(jsonObject.get("strInstructions").getAsString());
        if (jsonObject.has("strMealThumb")) meal.setThumbUrl(jsonObject.get("strMealThumb").getAsString());
        if (jsonObject.has("strYoutube") && !jsonObject.get("strYoutube").isJsonNull()) {
            meal.setYoutubeUrl(jsonObject.get("strYoutube").getAsString());
        }

        // 2. The Dynamic Loop (Extract Ingredients 1-20)
        for (int i = 1; i <= 20; i++) {
            String ingredientKey = "strIngredient" + i;
            String measureKey = "strMeasure" + i;

            if (jsonObject.has(ingredientKey)) {
                JsonElement ingElement = jsonObject.get(ingredientKey);
                JsonElement measureElement = jsonObject.get(measureKey);

                if (!ingElement.isJsonNull() && !ingElement.getAsString().trim().isEmpty()) {
                    String ingredientName = ingElement.getAsString();
                    String measure = (measureElement != null && !measureElement.isJsonNull()) ? measureElement.getAsString() : "";

                    // Add to our clean list
                    meal.addIngredient(new MealDetail.Ingredient(ingredientName, measure));
                }
            }
        }

        return meal;
    }
}