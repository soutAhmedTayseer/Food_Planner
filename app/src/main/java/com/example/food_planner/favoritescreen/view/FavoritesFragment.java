package com.example.food_planner.favoritescreen.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.SharedPrefManager;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private FavoritesAdapter adapter;
    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FavoritesAdapter();
        rvFavorites.setAdapter(adapter);

        // Initialize Manager
        sharedPrefManager = new SharedPrefManager(requireContext());

        // Load Data
        loadFavorites();
    }

    // Refresh data every time the fragment becomes visible (e.g. going back from another screen)
    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<MealDetail> list = sharedPrefManager.getFavorites();
        adapter.setList(list);
    }
}