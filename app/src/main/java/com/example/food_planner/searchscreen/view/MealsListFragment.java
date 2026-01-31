package com.example.food_planner.searchscreen.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
    private ImageButton btnBack; // Using ImageButton for the custom back arrow
    private MealsListAdapter adapter;
    private FoodApi foodApi;
    private CompositeDisposable disposable = new CompositeDisposable();

    // To hold arguments
    private String filterType;
    private String filterName;

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
            MealsListFragmentArgs args = MealsListFragmentArgs.fromBundle(getArguments());
            filterType = args.getFilterType();
            filterName = args.getFilterName();
        }

        // 2. Init Views
        recyclerView = view.findViewById(R.id.rvMealsList);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        TextView tvTitle = view.findViewById(R.id.tvListTitle);

        // Set Title if available
        if (filterName != null) tvTitle.setText(filterName + " Meals");
        
        // 4. Setup Recycler
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MealsListAdapter();
        recyclerView.setAdapter(adapter);

        // 5. Init API
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // 6. Fetch Data
        fetchMeals();

        // 7. Handle Item Click (Go to Details)
        adapter.setOnItemClickListener(mealId -> {
            fetchFullDetailsAndNavigate(mealId, view);
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    adapter.setList(response.getItems());
                }, error -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void fetchFullDetailsAndNavigate(String id, View view) {
        showLoading(true);
        // The list only has thumbnails/names. We need full details (instructions/video)
        disposable.add(foodApi.getMealById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        MealDetail detail = response.getMeals().get(0);

                        // Navigate to Details Fragment
                        MealsListFragmentDirections.ActionListToDetails action =
                                MealsListFragmentDirections.actionListToDetails(detail);
                        Navigation.findNavController(view).navigate(action);
                    }
                }, error -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                }));
    }

    private void showLoading(boolean isLoading) {
        // Safety check to ensure views exist
        if (lottieLoading != null && recyclerView != null) {
            if (isLoading) {
                lottieLoading.setVisibility(View.VISIBLE);
                lottieLoading.playAnimation();
                recyclerView.setVisibility(View.GONE);
            } else {
                lottieLoading.cancelAnimation();
                lottieLoading.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}