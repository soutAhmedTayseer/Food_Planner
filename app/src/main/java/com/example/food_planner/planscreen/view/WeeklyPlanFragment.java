package com.example.food_planner.planscreen.view;

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
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.planscreen.presenter.PlanPresenter;
import com.example.food_planner.planscreen.presenter.PlanPresenterImpl;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

public class WeeklyPlanFragment extends Fragment implements PlanView {

    private CalendarView calendarView;
    private RecyclerView rvPlanMeals;
    private TextView tvEmptyState, tvSelectedDateMeals;
    private View guestOverlay;

    private PlanMealsAdapter adapter;
    private PlanPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        // Initialize Presenter
        presenter = new PlanPresenterImpl(
                this,
                MealRepository.getInstance(requireContext()),
                new SharedPrefManager(requireContext())
        );

        presenter.checkMode();
        setupCalendarListener();
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        rvPlanMeals = view.findViewById(R.id.rvPlanMeals);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvSelectedDateMeals = view.findViewById(R.id.tvSelectedDateMeals);
        guestOverlay = view.findViewById(R.id.guestOverlay);
    }

    private void setupCalendarListener() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                presenter.selectDate(year, month, dayOfMonth)
        );
    }

    private void setupRecyclerView() {
        adapter = new PlanMealsAdapter(new PlanMealsAdapter.OnPlanClickListener() {
            @Override
            public void onMealClick(PlanMeal meal) {
                presenter.onMealClick(meal);
            }

            @Override
            public void onDeleteClick(PlanMeal meal) {
                presenter.onDeleteClick(meal);
            }
        });

        rvPlanMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlanMeals.setAdapter(adapter);
    }

    // --- View Interface Implementation ---

    @Override
    public void showGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.VISIBLE);
            guestOverlay.setOnClickListener(v -> showLoginDialog());
        }
        showLoginDialog();
    }

    @Override
    public void hideGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoginDialog() {
        AlertUtil.showLoginRequiredDialog(requireContext(), () -> presenter.onGuestLoginClick());
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void showPlanMeals(List<PlanMeal> meals) {
        rvPlanMeals.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        adapter.setList(meals);
    }

    @Override
    public void showEmptyState() {
        rvPlanMeals.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        adapter.setList(new ArrayList<>());
    }

    @Override
    public void updateDateHeader(String dateText) {
        tvSelectedDateMeals.setText(getString(R.string.meals_for_selected_date) + ": " + dateText);
    }

    @Override
    public void showDeleteConfirmation(PlanMeal meal) {
        AlertUtil.showConfirmationDialog(
                requireContext(),
                getString(R.string.remove_from_plan),
                getString(R.string.are_you_sure_you_want_to_remove_this_meal_from_your_favorites),
                () -> presenter.deletePlanConfirmed(meal)
        );
    }

    @Override
    public void showMessage(String message) {
        if (getView() != null) {
            SnackbarUtil.showSuccess(getView(), message);
        }
    }

    @Override
    public void showError(String error) {
        if (getContext() != null) {
            if (getView() != null) {
                SnackbarUtil.showError(getView(), error);
            } else {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void navigateToDetails(MealDetail mealDetail) {
        if (getView() != null) {
            WeeklyPlanFragmentDirections.ActionPlanToDetails action =
                    WeeklyPlanFragmentDirections.actionPlanToDetails(mealDetail);
            Navigation.findNavController(getView()).navigate(action);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}