package com.example.food_planner.homescreen.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
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
    private RecyclerView rvCarousel;

    // Data / Network
    private FoodApi foodApi;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private MealDetail currentMeal;
    private SharedPrefManager sharedPrefManager;
    private HomeCarouselAdapter carouselAdapter;

    // Auto Scroll Logic
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (rvCarousel != null && carouselAdapter != null && carouselAdapter.getActualItemCount() > 0) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvCarousel.getLayoutManager();
                if (layoutManager != null) {
                    int currentPosition = layoutManager.findFirstVisibleItemPosition();
                    rvCarousel.smoothScrollToPosition(currentPosition + 1);
                }
                // Schedule next scroll in 4 seconds
                sliderHandler.postDelayed(this, 4000);
            }
        }
    };

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
        setupCarousel();

        // 1. Daily Meal Logic
        checkDailyMealStatus();

        // 2. Random Carousel Logic
        fetchRandomInspirationMeals();
    }

    private void initViews(View view) {
        ivDailyMeal = view.findViewById(R.id.ivDailyMeal);
        tvDailyMealName = view.findViewById(R.id.tvDailyMealName);
        cardMealOfDay = view.findViewById(R.id.cardMealOfDay);
        layoutCardFront = view.findViewById(R.id.layout_card_front);
        layoutCardBack = view.findViewById(R.id.layout_card_back);
        rvCarousel = view.findViewById(R.id.rvCarousel);

        sharedPrefManager = new SharedPrefManager(requireContext());
    }

    private void setupApi() {
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);
    }

    private void setupCarousel() {
        carouselAdapter = new HomeCarouselAdapter();
        rvCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCarousel.setAdapter(carouselAdapter);

        // Click Listener for Carousel Items
        carouselAdapter.setOnItemClickListener(meal -> {
            navigateToDetails(meal);
        });
    }

    /**
     * Fetches 10 Random meals in Parallel using RxJava
     */
    private void fetchRandomInspirationMeals() {
        // Create a range of 10 emissions
        disposable.add(Observable.range(0, 10)
                .flatMapSingle(i -> foodApi.getRandomMeal()
                        .subscribeOn(Schedulers.io())) // Fetch each in background
                .toList() // Collect all results into a single List
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responses -> {
                    List<MealDetail> carouselMeals = new ArrayList<>();
                    for (com.example.food_planner.model.MealResponse response : responses) {
                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                            carouselMeals.add(response.getMeals().get(0));
                        }
                    }
                    // Update Adapter
                    carouselAdapter.setList(carouselMeals);

                    // Start Auto Scroll only if we have data
                    if (!carouselMeals.isEmpty()) {
                        // Start slightly offset to look better
                        rvCarousel.scrollToPosition(carouselMeals.size() * 100);
                        sliderHandler.removeCallbacks(sliderRunnable);
                        sliderHandler.postDelayed(sliderRunnable, 3000);
                    }

                }, error -> Log.e(TAG, "Carousel Error: " + error.getMessage())));
    }

    // --- Daily Meal Logic ---

    private void checkDailyMealStatus() {
        MealDetail savedMeal = sharedPrefManager.getValidDailyMeal();

        if (savedMeal != null) {
            currentMeal = savedMeal;
            tvDailyMealName.setText(currentMeal.getName());
            Glide.with(this).load(currentMeal.getThumbUrl())
                    .placeholder(R.drawable.ic_launcher_background).into(ivDailyMeal);

            layoutCardFront.setVisibility(View.GONE);
            layoutCardBack.setVisibility(View.VISIBLE);
            cardMealOfDay.setOnClickListener(v -> navigateToDetails(currentMeal));
        } else {
            showMysteryState();
        }
    }

    private void showMysteryState() {
        layoutCardFront.setVisibility(View.VISIBLE);
        layoutCardBack.setVisibility(View.GONE);
        cardMealOfDay.setOnClickListener(v -> fetchAndFlip());
    }

    private void fetchAndFlip() {
        cardMealOfDay.setClickable(false);

        disposable.add(foodApi.getRandomMeal().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                currentMeal = response.getMeals().get(0);
                sharedPrefManager.saveDailyMeal(currentMeal);

                tvDailyMealName.setText(currentMeal.getName());
                Glide.with(this).load(currentMeal.getThumbUrl()).into(ivDailyMeal);

                performFlipAnimation();
            } else {
                showError(getString(R.string.failed_to_load_daily_meal));
                cardMealOfDay.setClickable(true);
            }
        }, error -> {
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
                cardMealOfDay.setOnClickListener(v -> navigateToDetails(currentMeal));
            }).start();
        }).start();
    }

    private void navigateToDetails(MealDetail meal) {
        if (meal != null) {
            HomeFragmentDirections.ActionHomeToMealDetails action = HomeFragmentDirections.actionHomeToMealDetails(meal);
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause auto-scroll when fragment is not visible
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume auto-scroll
        sliderHandler.postDelayed(sliderRunnable, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}