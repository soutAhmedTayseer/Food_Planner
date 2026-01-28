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
import androidx.navigation.Navigation; // Import Navigation
import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail; // Use MealDetail (Full), not MealItem
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    private ImageView ivDailyMeal;
    private TextView tvDailyMealName;
    private View cardMealOfDay;

    private FoodApi foodApi;
    private CompositeDisposable disposable = new CompositeDisposable();

    // Store the fetched meal so we can pass it on click
    private MealDetail currentRandomMeal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        ivDailyMeal = view.findViewById(R.id.ivDailyMeal);
        tvDailyMealName = view.findViewById(R.id.tvDailyMealName);
        cardMealOfDay = view.findViewById(R.id.cardMealOfDay);

        // 2. Initialize Network
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);

        // 3. Fetch Data
        getMealOfTheDay();
    }

    private void getMealOfTheDay() {
        // Now returns Single<MealResponse>
        disposable.add(foodApi.getRandomMeal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        // Capture the full details
                        currentRandomMeal = response.getMeals().get(0);
                        updateUI(currentRandomMeal);
                    }
                }, error -> {
                    Log.e(getString(R.string.homefragment), getString(R.string.error) + error.getMessage());
                    Toast.makeText(getContext(), R.string.failed_to_load_daily_meal, Toast.LENGTH_SHORT).show();
                }));
    }

    private void updateUI(MealDetail meal) {
        // Set Name
        tvDailyMealName.setText(meal.getName());

        // Set Image
        Glide.with(this)
                .load(meal.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivDailyMeal);

        // Set Click Listener -> Navigate to MealDetailsFragment
        cardMealOfDay.setOnClickListener(v -> {
            if (currentRandomMeal != null) {
                // Use Safe Args to pass the full object
                HomeFragmentDirections.ActionHomeToMealDetails action =
                        HomeFragmentDirections.actionHomeToMealDetails(currentRandomMeal);
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}