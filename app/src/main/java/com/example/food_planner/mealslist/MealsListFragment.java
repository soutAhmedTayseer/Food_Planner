package com.example.food_planner.mealslist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.MealItem;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.searchscreen.view.MealsListFragmentDirections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
    private SearchView searchView;
    private TextView tvTitle;
    private MealsListAdapter adapter;
    private FoodApi foodApi;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // Data handling
    private String filterType;
    private String filterName;
    private List<MealItem> originalList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meals_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get Arguments
        if (getArguments() != null) {
            com.example.food_planner.searchscreen.view.MealsListFragmentArgs args = com.example.food_planner.searchscreen.view.MealsListFragmentArgs.fromBundle(getArguments());
            filterType = args.getFilterType();
            filterName = args.getFilterName();
        }

        // 2. Init Views
        recyclerView = view.findViewById(R.id.rvMealsList);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        searchView = view.findViewById(R.id.searchView);
        tvTitle = view.findViewById(R.id.tvListTitle);

        // 3. UI Setup
        if (filterName != null) {
            tvTitle.setText(filterName); // e.g. "Seafood" or "Italian"
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MealsListAdapter();
        recyclerView.setAdapter(adapter);

        // 4. Init API
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // 5. Fetch Data
        fetchMeals();

        // 6. Setup Search Filter (Local Filtering)
        setupSearchFilter();

        // 7. Handle Item Click (Go to Details)
        adapter.setOnItemClickListener((mealId, sharedImage) -> {
            fetchFullDetailsAndNavigate(mealId, view, sharedImage);
        });
    }

    private void fetchMeals() {
        showLoading(true);
        // Determine which API endpoint to call based on filter type
        io.reactivex.rxjava3.core.Single<com.example.food_planner.model.Meal> apiCall;

        if ("c".equals(filterType)) {
            apiCall = foodApi.filterByCategory(filterName);
        } else if ("a".equals(filterType)) {
            apiCall = foodApi.filterByArea(filterName);
        } else {
            apiCall = foodApi.filterByIngredient(filterName);
        }

        disposable.add(apiCall
                .delay(500, TimeUnit.MILLISECONDS) // Slight delay for smooth animation
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    if (response.getItems() != null) {
                        originalList = response.getItems();
                        adapter.setList(originalList);
                    }
                }, error -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void setupSearchFilter() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String query) {
        if (originalList == null || originalList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            adapter.setList(new ArrayList<>(originalList));
        } else {
            List<MealItem> filtered = originalList.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.setList(filtered);
        }
        // Scroll top to see results nicely
        recyclerView.scrollToPosition(0);
    }

    private void fetchFullDetailsAndNavigate(String id, View view, View sharedImage) {
        // Show lightweight loading or just wait (optional UI feedback here)
        disposable.add(foodApi.getMealById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        MealDetail detail = response.getMeals().get(0);

                        // Navigation with Shared Element Transition Support
                        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                                .addSharedElement(sharedImage, "shared_image")
                                .build();

                        MealsListFragmentDirections.ActionListToDetails action =
                                MealsListFragmentDirections.actionListToDetails(detail);
                        Navigation.findNavController(view).navigate(action, extras);
                    }
                }, error -> {
                    Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                }));
    }

    private void showLoading(boolean isLoading) {
        if (lottieLoading == null || recyclerView == null) return;

        if (isLoading) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            // Smooth fade out
            recyclerView.animate().alpha(0f).setDuration(200).withEndAction(() -> recyclerView.setVisibility(View.GONE));
        } else {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // Smooth fade in
            recyclerView.animate().alpha(1f).setDuration(300);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}