package com.example.food_planner.favoritescreen.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.favoritescreen.view.FavoritesAdapter;
import com.example.food_planner.favoritescreen.view.FavoritesFragmentDirections;
import com.example.food_planner.repository.MealRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private FavoritesAdapter adapter;
    private MealRepository mealRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Repository
        mealRepository = new MealRepository(requireContext());

        // Setup RecyclerView
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter();
        rvFavorites.setAdapter(adapter);

        // 1. Navigation on Click
        adapter.setOnFavItemClickListener(meal -> {
            FavoritesFragmentDirections.ActionFavoritesToMealDetails action = FavoritesFragmentDirections.actionFavoritesToMealDetails(meal);
            Navigation.findNavController(view).navigate(action);
        });

        // 2. Load Data from Room
        loadFavorites();
    }

    private void loadFavorites() {
        disposable.add(mealRepository.getStoredMeals().observeOn(AndroidSchedulers.mainThread()).subscribe(meals -> adapter.setList(meals), error -> Toast.makeText(getContext(), "Error loading favorites", Toast.LENGTH_SHORT).show()));
    }

    // Clean up RxJava
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}