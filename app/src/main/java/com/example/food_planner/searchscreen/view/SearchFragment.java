package com.example.food_planner.searchscreen.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.List;
import java.util.concurrent.TimeUnit; // Import needed for delay

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
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

        // 1. Initialize Views
        recyclerView = view.findViewById(R.id.rvSearch);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        chipGroup = view.findViewById(R.id.chipGroupSearch);

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        // 3. Initialize API
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // 4. Track current type to handle navigation correctly
        final String[] currentType = {"c"};

        // 5. Load Default Data (Categories)
        loadCategories();

        // 6. Handle Chip Selection
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
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

        // 7. Handle Item Click with Shared Element Transition
        adapter.setOnItemClickListener((itemName, sharedImageView) -> {
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(sharedImageView, "shared_image")
                    .build();

            SearchFragmentDirections.ActionSearchToList action =
                    SearchFragmentDirections.actionSearchToList(currentType[0], itemName);

            Navigation.findNavController(view).navigate(action, extras);
        });
    }

    // --- Network Calls with 1 Second Delay ---

    private void loadCategories() {
        showLoading(true);
        disposable.add(foodApi.getCategories()
                .delay(1, TimeUnit.SECONDS) // Force 1-second wait for animation
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    updateList(response.getItems());
                }, error -> {
                    showLoading(false);
                    showError(error.getMessage());
                }));
    }

    private void loadCountries() {
        showLoading(true);
        disposable.add(foodApi.getAreas()
                .delay(1, TimeUnit.SECONDS) // Force 1-second wait for animation
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    showLoading(false);
                    updateList(response.getItems());
                }, error -> {
                    showLoading(false);
                    showError(error.getMessage());
                }));
    }

    private void loadIngredients() {
        showLoading(true);
        disposable.add(foodApi.getIngredients()
                .delay(1, TimeUnit.SECONDS) // Force 1-second wait for animation
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
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
        // Ensure views exist before accessing them
        if (lottieLoading == null || recyclerView == null) return;

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

    private void showError(String msg) {
        SnackbarUtil.showError(getView(), msg);
        Log.e("API_ERROR", msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}