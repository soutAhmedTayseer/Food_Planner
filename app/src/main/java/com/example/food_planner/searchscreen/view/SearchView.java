package com.example.food_planner.searchscreen.view;

import com.example.food_planner.model.MealItem;
import java.util.List;

public interface SearchView {
    void showLoading();
    void hideLoading();
    void showData(List<MealItem> items);
    void showError(String message);
}