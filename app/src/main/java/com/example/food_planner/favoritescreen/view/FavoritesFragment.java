package com.example.food_planner.favoritescreen.view;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.food_planner.repository.MealRepository;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SharedPrefManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private FavoritesAdapter adapter;
    private MealRepository mealRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // Guest Mode Variables
    private View guestOverlay;
    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize SharedPrefs and Overlay
        sharedPrefManager = new SharedPrefManager(requireContext());
        guestOverlay = view.findViewById(R.id.guestOverlay);

        // 2. Check Guest Status
        if (sharedPrefManager.isGuest()) {
            setupGuestMode();
        } else {
            // Only load data if NOT a guest
            setupUserMode(view);
        }
    }

    private void setupGuestMode() {
        // Show the blur overlay
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.VISIBLE);
        }

        // Show the Alert Dialog
        AlertUtil.showLoginRequiredDialog(requireContext(), () -> {
            // Logic for "Login" button click
            sharedPrefManager.logoutUser();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupUserMode(View view) {
        // Hide overlay
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.GONE);
        }

        // --- ORIGINAL LOGIC STARTS HERE ---
        mealRepository = new MealRepository(requireContext());

        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter();
        rvFavorites.setAdapter(adapter);

        // Navigation
        adapter.setOnFavItemClickListener(meal -> {
            FavoritesFragmentDirections.ActionFavoritesToMealDetails action =
                    FavoritesFragmentDirections.actionFavoritesToMealDetails(meal);
            Navigation.findNavController(view).navigate(action);
        });

        // Load Data
        loadFavorites();
    }

    private void loadFavorites() {
        disposable.add(mealRepository.getStoredMeals()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> adapter.setList(meals),
                        error -> Toast.makeText(getContext(), "Error loading favorites", Toast.LENGTH_SHORT).show()
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}