package com.example.food_planner.searchscreen.presenter;

public interface SearchPresenter {
    // Determine which API call to make based on the chip type ("c", "a", or "i")
    void searchByType(String type);

    // Filter the already loaded list based on the search bar text
    void searchLocalList(String query);

    // Cleanup
    void onDestroy();
}