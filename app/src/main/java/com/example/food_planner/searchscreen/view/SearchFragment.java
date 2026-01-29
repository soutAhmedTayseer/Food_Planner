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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.model.MealItem;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.utils.ViewUtils;
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

        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // --- 1. Track the current type ("c"=Category, "a"=Area, "i"=Ingredient) ---
        // We use an array so we can change it inside the listeners below
        final String[] currentType = {"c"};

        // Load Default
        loadCategories();

        // --- 2. Update currentType when chips are clicked ---
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCategory) {
                currentType[0] = "c"; // Set type to Category
                loadCategories();
            } else if (checkedId == R.id.chipCountry) {
                currentType[0] = "a"; // Set type to Area
                loadCountries();
            } else if (checkedId == R.id.chipIngredient) {
                currentType[0] = "i"; // Set type to Ingredient
                loadIngredients();
            }
        });

        // --- 3. Handle Item Click (Now the method exists!) ---
        adapter.setOnItemClickListener(itemName -> {
            // Navigate to List, passing the type (e.g., "c") and name (e.g., "Beef")
            SearchFragmentDirections.ActionSearchToList action = SearchFragmentDirections.actionSearchToList(currentType[0], itemName);
            Navigation.findNavController(view).navigate(action);
        });
    }

    // --- Network Calls (Unchanged) ---

    private void loadCategories() {
        showLoading(true);
        disposable.add(foodApi.getCategories().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            showLoading(false);
            updateList(response.getItems());
        }, error -> {
            showLoading(false);
            showError(error.getMessage());
        }));
    }

    private void loadCountries() {
        showLoading(true);
        disposable.add(foodApi.getAreas().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            showLoading(false);
            updateList(response.getItems());
        }, error -> {
            showLoading(false);
            showError(error.getMessage());
        }));
    }

    private void loadIngredients() {
        showLoading(true);
        disposable.add(foodApi.getIngredients().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            showLoading(false);
            updateList(response.getItems());
        }, error -> {
            showLoading(false);
            showError(error.getMessage());
        }));
    }

    private void updateList(List<MealItem> items) {
        adapter.setList(items);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        ViewUtils.showError(getView(), msg);
        Log.e("API_ERROR", msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}