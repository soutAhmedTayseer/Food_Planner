package com.example.food_planner.homescreen.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.utils.SharedPrefManager; // Import your Manager
import com.google.android.material.card.MaterialCardView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private ImageView ivDailyMeal;
    private TextView tvDailyMealName;
    private MaterialCardView cardMealOfDay;
    private View layoutCardFront;
    private View layoutCardBack;

    // Data / Network
    private FoodApi foodApi;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private MealDetail currentMeal;
    private SharedPrefManager sharedPrefManager; // Use the manager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupApi();

        // Use Manager to check for valid meal
        checkDailyMealStatus();
    }

    private void initViews(View view) {
        ivDailyMeal = view.findViewById(R.id.ivDailyMeal);
        tvDailyMealName = view.findViewById(R.id.tvDailyMealName);
        cardMealOfDay = view.findViewById(R.id.cardMealOfDay);
        layoutCardFront = view.findViewById(R.id.layout_card_front);
        layoutCardBack = view.findViewById(R.id.layout_card_back);

        // Initialize Manager
        sharedPrefManager = new SharedPrefManager(requireContext());
    }

    private void setupApi() {
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);
    }

    private void checkDailyMealStatus() {
        // BUG FIX: Instead of manually getting strings and making a new object,
        // we ask the manager for the full object if it's valid.
        MealDetail savedMeal = sharedPrefManager.getValidDailyMeal();

        if (savedMeal != null) {
            // Meal exists and is valid -> Show it (Revealed State)
            currentMeal = savedMeal;

            // Populate UI
            tvDailyMealName.setText(currentMeal.getName()); // Assuming getName() exists
            Glide.with(this).load(currentMeal.getThumbUrl()) // Assuming getThumbUrl() exists
                    .placeholder(R.drawable.ic_launcher_background).into(ivDailyMeal);

            // Show Back immediately
            layoutCardFront.setVisibility(View.GONE);
            layoutCardBack.setVisibility(View.VISIBLE);

            // Click listener: Navigate
            cardMealOfDay.setOnClickListener(v -> navigateToDetails());
        } else {
            // Expired or Empty -> Show Mystery State
            showMysteryState();
        }
    }

    private void showMysteryState() {
        layoutCardFront.setVisibility(View.VISIBLE);
        layoutCardBack.setVisibility(View.GONE);

        // Click listener: Fetch new meal and Flip
        cardMealOfDay.setOnClickListener(v -> fetchAndFlip());
    }

    private void fetchAndFlip() {
        cardMealOfDay.setClickable(false); // Prevent double clicks

        disposable.add(foodApi.getRandomMeal().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                currentMeal = response.getMeals().get(0);

                // Save using Manager
                sharedPrefManager.saveDailyMeal(currentMeal);

                // Populate UI before flip
                tvDailyMealName.setText(currentMeal.getName());
                Glide.with(this).load(currentMeal.getThumbUrl()).into(ivDailyMeal);

                // Flip
                performFlipAnimation();
            } else {
                showError(getString(R.string.failed_to_load_daily_meal));
                cardMealOfDay.setClickable(true);
            }
        }, error -> {
            Log.e(TAG, "Error: " + error.getMessage());
            showError(getString(R.string.error) + " " + error.getMessage());
            cardMealOfDay.setClickable(true);
        }));
    }

    private void performFlipAnimation() {
        final float scale = requireContext().getResources().getDisplayMetrics().density;
        cardMealOfDay.setCameraDistance(8000 * scale);

        layoutCardFront.animate().withLayer().rotationY(90).setDuration(300).withEndAction(() -> {
            layoutCardFront.setVisibility(View.GONE);
            layoutCardBack.setVisibility(View.VISIBLE);
            layoutCardBack.setRotationY(-90);

            layoutCardBack.animate().withLayer().rotationY(0).setDuration(300).withEndAction(() -> {
                cardMealOfDay.setClickable(true);
                cardMealOfDay.setOnClickListener(v -> navigateToDetails());
            }).start();
        }).start();
    }

    private void navigateToDetails() {
        if (currentMeal != null) {
            HomeFragmentDirections.ActionHomeToMealDetails action = HomeFragmentDirections.actionHomeToMealDetails(currentMeal);
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}