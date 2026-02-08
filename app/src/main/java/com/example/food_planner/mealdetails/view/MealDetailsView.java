package com.example.food_planner.mealdetails.view;

import com.example.food_planner.model.MealDetail;
import java.util.List;

public interface MealDetailsView {
    void showMealInfo(String name, String area, String category, String thumbUrl);
    void showIngredients(List<MealDetail.Ingredient> ingredients);
    void showInstructions(List<String> steps);
    void showVideo(String videoId);
    void hideVideo();
    void setFavoriteState(boolean isFavorite);
    void showMessage(String message);
    void showError(String error);
    void showRemoveConfirmationDialog();
}