package com.example.food_planner.planscreen.view;

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
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.repository.MealRepository;
import com.example.food_planner.utils.AlertUtil; // Import AlertUtil
import com.example.food_planner.utils.SnackbarUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WeeklyPlanFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvPlanMeals;
    private TextView tvEmptyState, tvSelectedDateMeals;

    private MealRepository repository;
    private PlanMealsAdapter adapter;
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

        repository = new MealRepository(requireContext());
        initViews(view);
        setupRecyclerView();

        // 1. Set Initial Date to Today
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        selectedDate = sdf.format(new Date());
        updateSelectedDateHeader(selectedDate);
        loadMealsForDate(selectedDate);

        // 2. Calendar Selection Logic
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            updateSelectedDateHeader(selectedDate);
            loadMealsForDate(selectedDate);
        });
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        rvPlanMeals = view.findViewById(R.id.rvPlanMeals);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvSelectedDateMeals = view.findViewById(R.id.tvSelectedDateMeals);
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
                // SHOW ALERT before deleting from plan
                AlertUtil.showConfirmationDialog(requireContext(), getString(R.string.remove_from_plan), "Are you sure you want to remove this meal from your schedule?", () -> deletePlan(meal) // Callback to actual delete logic
                );
            }
        });

        rvPlanMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlanMeals.setAdapter(adapter);
    }

    private void loadMealsForDate(String date) {
        disposable.add(repository.getPlansByDate(date).observeOn(AndroidSchedulers.mainThread()).subscribe(meals -> {
            if (meals.isEmpty()) {
                rvPlanMeals.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvPlanMeals.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
                adapter.setList(meals);
            }
        }, error -> Toast.makeText(getContext(), "Error loading plan", Toast.LENGTH_SHORT).show()));
    }

    private void deletePlan(PlanMeal meal) {
        disposable.add(repository.removeFromPlan(meal).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> SnackbarUtil.showSuccess(getView(), "Meal removed"), error -> SnackbarUtil.showError(getView(), "Delete failed")));
    }

    private void updateSelectedDateHeader(String date) {
        tvSelectedDateMeals.setText("Meals for " + date);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}