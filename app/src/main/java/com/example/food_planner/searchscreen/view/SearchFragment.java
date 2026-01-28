package com.example.food_planner.searchscreen.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.model.MealItem;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ChipGroup chipGroup;
    private SearchAdapter adapter;
    private FoodApi foodApi;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvSearch);
        progressBar = view.findViewById(R.id.progressBar);
        chipGroup = view.findViewById(R.id.chipGroupSearch);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize API
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // Load Default (Categories)
        loadCategories();

        // Handle Chip Clicks
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCategory) {
                loadCategories();
            } else if (checkedId == R.id.chipCountry) {
                loadCountries();
            } else if (checkedId == R.id.chipIngredient) {
                loadIngredients();
            }
        });
    }

    // --- Network Calls ---

    private void loadCategories() {
        showLoading(true);
        disposable.add(foodApi.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            showLoading(false);
                            // FIX: Use getItems() because JSON key is "meals"
                            updateList(response.getItems());
                        },
                        error -> {
                            showLoading(false);
                            showError(error.getMessage());
                        }
                ));
    }

    private void loadCountries() {
        showLoading(true);
        disposable.add(foodApi.getAreas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            showLoading(false);
                            updateList(response.getItems());
                        },
                        error -> {
                            showLoading(false);
                            showError(error.getMessage());
                        }
                ));
    }

    private void loadIngredients() {
        showLoading(true);
        disposable.add(foodApi.getIngredients()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            showLoading(false);
                            updateList(response.getItems());
                        },
                        error -> {
                            showLoading(false);
                            showError(error.getMessage());
                        }
                ));
    }

    private void updateList(List<MealItem> items) {
        adapter.setList(items);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e("API_ERROR", msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}