package com.example.food_planner.favoritescreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private TextView tvEmptyState;
    private View guestOverlay;

    private FavoritesAdapter adapter;
    private MealRepository mealRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        sharedPrefManager = new SharedPrefManager(requireContext());

        if (sharedPrefManager.isGuest()) {
            setupGuestMode();
        } else {
            setupUserMode(view);
        }
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        guestOverlay = view.findViewById(R.id.guestOverlay);
    }

    private void setupGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.VISIBLE);

            // Make the entire overlay clickable to show the login dialog
            guestOverlay.setOnClickListener(v -> showGuestLoginDialog());
        }
        // Optionally prompt immediately
        showGuestLoginDialog();
    }

    private void showGuestLoginDialog() {
        AlertUtil.showLoginRequiredDialog(requireContext(), () -> {
            sharedPrefManager.logoutUser();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupUserMode(View view) {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.GONE);
        }

        mealRepository = new MealRepository(requireContext());

        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter();
        rvFavorites.setAdapter(adapter);

        // Navigation
        adapter.setOnFavItemClickListener(meal -> {
            FavoritesFragmentDirections.ActionFavoritesToMealDetails action =
                    FavoritesFragmentDirections.actionFavoritesToMealDetails(meal);
            Navigation.findNavController(view).navigate(action);
        });

        loadFavorites();
    }

    private void loadFavorites() {
        disposable.add(mealRepository.getStoredMeals()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> {
                            if (meals == null || meals.isEmpty()) {
                                // NO FAVORITES: Show Empty State
                                rvFavorites.setVisibility(View.GONE);
                                tvEmptyState.setVisibility(View.VISIBLE);
                                adapter.setList(new ArrayList<>());
                            } else {
                                // FAVORITES EXIST: Show Recycler
                                rvFavorites.setVisibility(View.VISIBLE);
                                tvEmptyState.setVisibility(View.GONE);
                                adapter.setList(meals);
                            }
                        },
                        error -> {
                            Toast.makeText(getContext(), "Error loading favorites", Toast.LENGTH_SHORT).show();
                            // Fallback to empty state on error
                            rvFavorites.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}