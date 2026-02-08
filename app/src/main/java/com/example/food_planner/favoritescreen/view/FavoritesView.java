package com.example.food_planner.favoritescreen.view;

import com.example.food_planner.model.MealDetail;
import java.util.List;

public interface FavoritesView {
    void showFavorites(List<MealDetail> meals);
    void showEmptyState();
    void showGuestMode();
    void hideGuestMode();
    void navigateToLogin();
    void navigateToDetails(MealDetail meal);
    void showError(String message);
}