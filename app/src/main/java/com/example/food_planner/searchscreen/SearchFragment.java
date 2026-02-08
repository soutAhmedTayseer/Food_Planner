package com.example.food_planner.searchscreen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.food_planner.model.MealItem;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
    private ChipGroup chipGroup;
    private SearchView searchView;
    private SearchAdapter adapter;
    private FoodApi foodApi;
    private final CompositeDisposable disposable = new CompositeDisposable();

    // 1. Holds the full data from API (before filtering)
    private List<MealItem> originalList = new ArrayList<>();

    // 2. RxJava Subject for Search Events
    private final PublishSubject<String> searchSubject = PublishSubject.create();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        recyclerView = view.findViewById(R.id.rvSearch);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        chipGroup = view.findViewById(R.id.chipGroupSearch);
        searchView = view.findViewById(R.id.searchView);

        // 2. Setup RecyclerView with Animation support
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        // Optional: Optimize RecyclerView
        recyclerView.setHasFixedSize(true);

        // 3. Initialize API
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // 4. Setup Search Logic (RxJava)
        setupSearchObserver();

        // 5. Connect SearchView to RxJava
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchSubject.onNext(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchSubject.onNext(newText);
                    return true;
                }
            });
        }

        // 6. Track current type
        final String[] currentType = {"c"};

        // 7. Load Default Data (Categories)
        loadCategories();

        // 8. Handle Chip Selection
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Clear search when switching tabs so user sees the full new list
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }

            if (checkedId == R.id.chipCategory) {
                currentType[0] = "c";
                loadCategories();
            } else if (checkedId == R.id.chipCountry) {
                currentType[0] = "a";
                loadCountries();
            } else if (checkedId == R.id.chipIngredient) {
                currentType[0] = "i";
                loadIngredients();
            }
        });

        // 9. Handle Item Click
        adapter.setOnItemClickListener((itemName, sharedImageView) -> {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(sharedImageView, "shared_image")
                    .build();

            SearchFragmentDirections.ActionSearchToList action =
                    SearchFragmentDirections.actionSearchToList(currentType[0], itemName);

            Navigation.findNavController(view).navigate(action, extras);
        });
    }

    // --- Search Logic ---

    private void setupSearchObserver() {
        disposable.add(searchSubject
                .debounce(300, TimeUnit.MILLISECONDS) // Wait 300ms for user to stop typing
                .distinctUntilChanged() // Ignore if text is same as before
                .observeOn(AndroidSchedulers.mainThread()) // Update UI on Main Thread
                .subscribe(query -> {
                    filterList(query);
                }, error -> Log.e("Search", "Error", error)));
    }

    private void filterList(String query) {
        if (originalList == null || originalList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            // No text? Show everything
            adapter.setList(new ArrayList<>(originalList));
        } else {
            // Filter the original list (Case insensitive)
            List<MealItem> filtered = originalList.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.setList(filtered);
        }

        // Scroll to top when list updates to make animation look good
        recyclerView.scrollToPosition(0);
    }

    // --- Network Calls ---

    private void loadCategories() {
        showLoading(true);
        disposable.add(foodApi.getCategories()
                // Added slight delay to ensure Lottie is seen before animation triggers
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    originalList = response.getItems();
                    filterList(searchView != null ? searchView.getQuery().toString() : "");
                }, error -> {
                    showLoading(false);
                    showError(error.getMessage());
                }));
    }

    private void loadCountries() {
        showLoading(true);
        disposable.add(foodApi.getAreas()
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    originalList = response.getItems();
                    filterList(searchView != null ? searchView.getQuery().toString() : "");
                }, error -> {
                    showLoading(false);
                    showError(error.getMessage());
                }));
    }

    private void loadIngredients() {
        showLoading(true);
        disposable.add(foodApi.getIngredients()
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    originalList = response.getItems();
                    filterList(searchView != null ? searchView.getQuery().toString() : "");
                }, error -> {
                    showLoading(false);
                    showError(error.getMessage());
                }));
    }

    private void showLoading(boolean isLoading) {
        if (lottieLoading == null || recyclerView == null) return;

        if (isLoading) {
            // Fade logic for smooth transition
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            recyclerView.animate().alpha(0f).setDuration(200).withEndAction(() -> recyclerView.setVisibility(View.GONE));
        } else {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.animate().alpha(1f).setDuration(300);
        }
    }

    private void showError(String msg) {
        if (getView() != null) {
            SnackbarUtil.showError(getView(), msg);
            Log.e("API_ERROR", msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}