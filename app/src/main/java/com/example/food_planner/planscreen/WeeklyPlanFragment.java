package com.example.food_planner.planscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WeeklyPlanFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvPlanMeals;
    private TextView tvEmptyState, tvSelectedDateMeals;
    private View guestOverlay;

    private MealRepository mealRepository;
    private PlanMealsAdapter adapter;
    private SharedPrefManager sharedPrefManager;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private String selectedDate; // Format YYYY-MM-DD

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        sharedPrefManager = new SharedPrefManager(requireContext());

        if (sharedPrefManager.isGuest()) {
            setupGuestMode();
        } else {
            setupUserMode();
        }
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        rvPlanMeals = view.findViewById(R.id.rvPlanMeals);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvSelectedDateMeals = view.findViewById(R.id.tvSelectedDateMeals);
        guestOverlay = view.findViewById(R.id.guestOverlay);
    }

    private void setupGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.VISIBLE);

            // Make the entire overlay clickable to show the login dialog
            guestOverlay.setOnClickListener(v -> showGuestLoginDialog());
        }
        // Prompt immediately
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

    private void setupUserMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.GONE);
        }

        mealRepository = MealRepository.getInstance(requireContext());
        setupRecyclerView();

        // 1. Set Initial Date to Today
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        selectedDate = sdf.format(new Date());
        updateSelectedDateHeader(selectedDate);
        loadMealsForDate(selectedDate);

        // 2. Calendar Selection Logic
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Month is 0-indexed in Android CalendarView
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            updateSelectedDateHeader(selectedDate);
            loadMealsForDate(selectedDate);
        });
    }

    private void setupRecyclerView() {
        adapter = new PlanMealsAdapter(new PlanMealsAdapter.OnPlanClickListener() {
            @Override
            public void onMealClick(PlanMeal meal) {
                WeeklyPlanFragmentDirections.ActionPlanToDetails action = WeeklyPlanFragmentDirections.actionPlanToDetails(meal.toMealDetail());
                Navigation.findNavController(requireView()).navigate(action);
            }

            @Override
            public void onDeleteClick(PlanMeal meal) {
                AlertUtil.showConfirmationDialog(
                        requireContext(),
                        getString(R.string.remove_from_plan),
                        getString(R.string.are_you_sure_you_want_to_remove_this_meal_from_your_favorites), // Reusing string as requested
                        () -> deletePlan(meal)
                );
            }
        });

        rvPlanMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlanMeals.setAdapter(adapter);
    }

    private void loadMealsForDate(String date) {
        disposable.add(mealRepository.getPlansByDate(date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meals -> {
                    if (meals == null || meals.isEmpty()) {
                        // NO MEALS: Show Empty State
                        rvPlanMeals.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        adapter.setList(new ArrayList<>());
                    } else {
                        // MEALS FOUND: Show RecyclerView
                        rvPlanMeals.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                        adapter.setList(meals);
                    }
                }, error -> {
                    Toast.makeText(getContext(), "Error loading plan", Toast.LENGTH_SHORT).show();
                    // Fallback to empty state on error
                    rvPlanMeals.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                }));
    }

    private void deletePlan(PlanMeal meal) {
        disposable.add(mealRepository.removeFromPlan(meal)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            SnackbarUtil.showSuccess(getView(), getString(R.string.remove));
                            // Reload list to refresh empty state if last item deleted
                            loadMealsForDate(selectedDate);
                        },
                        error -> SnackbarUtil.showError(getView(), "Delete failed")
                ));
    }

    private void updateSelectedDateHeader(String date) {
        tvSelectedDateMeals.setText(getString(R.string.meals_for_selected_date) + ": " + date);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}