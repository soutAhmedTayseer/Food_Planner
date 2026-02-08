package com.example.food_planner.planscreen.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.model.PlanMeal;
import com.example.food_planner.planscreen.view.PlanView;
import com.example.food_planner.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PlanPresenterImpl implements PlanPresenter {

    private final PlanView view;
    private final MealRepository repository;
    private final SharedPrefManager sharedPrefManager;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private String selectedDate; // Format YYYY-MM-DD

    public PlanPresenterImpl(PlanView view, MealRepository repository, SharedPrefManager sharedPrefManager) {
        this.view = view;
        this.repository = repository;
        this.sharedPrefManager = sharedPrefManager;
    }

    @Override
    public void checkMode() {
        if (sharedPrefManager.isGuest()) {
            view.showGuestMode();
        } else {
            view.hideGuestMode();
            // Set Initial Date to Today
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            selectedDate = sdf.format(new Date());
            view.updateDateHeader(selectedDate);
            getMealsForDate(selectedDate);
        }
    }

    @Override
    public void selectDate(int year, int month, int dayOfMonth) {
        // Month is 0-indexed in Android CalendarView
        selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
        view.updateDateHeader(selectedDate);
        getMealsForDate(selectedDate);
    }

    @Override
    public void getMealsForDate(String date) {
        disposable.add(repository.getPlansByDate(date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meals -> {
                    if (meals == null || meals.isEmpty()) {
                        view.showEmptyState();
                    } else {
                        view.showPlanMeals(meals);
                    }
                }, error -> {
                    view.showError("Error loading plan");
                    view.showEmptyState();
                }));
    }

    @Override
    public void onMealClick(PlanMeal meal) {
        // Convert PlanMeal to MealDetail for navigation logic
        if (meal != null) {
            view.navigateToDetails(meal.toMealDetail());
        }
    }

    @Override
    public void onDeleteClick(PlanMeal meal) {
        view.showDeleteConfirmation(meal);
    }

    @Override
    public void deletePlanConfirmed(PlanMeal meal) {
        disposable.add(repository.removeFromPlan(meal)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            view.showMessage("Removed");
                            // Reload list to refresh empty state if needed
                            getMealsForDate(selectedDate);
                        },
                        error -> view.showError("Delete failed")
                ));
    }

    @Override
    public void onGuestLoginClick() {
        sharedPrefManager.logoutUser();
        view.navigateToLogin();
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}