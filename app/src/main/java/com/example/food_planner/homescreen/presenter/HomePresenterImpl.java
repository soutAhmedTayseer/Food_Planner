package com.example.food_planner.homescreen.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.homescreen.view.HomeView;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.MealResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenterImpl implements HomePresenter {

    private final HomeView view;
    private final MealRepository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public HomePresenterImpl(HomeView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void getDailyMeal() {
        MealDetail savedMeal = repository.getValidDailyMeal();
        if (savedMeal != null) {
            view.showDailyMeal(savedMeal);
        } else {
            view.showMysteryCard();
        }
    }

    @Override
    public void requestNewDailyMeal() {
        disposable.add(repository.getRandomMeal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        MealDetail meal = response.getMeals().get(0);
                        repository.saveDailyMeal(meal);
                        view.animateFlipToMeal(meal);
                    } else {
                        view.showError("Failed to load meal");
                    }
                }, error -> view.showError(error.getMessage())));
    }

    @Override
    public void getInspirationMeals(boolean forceRefresh) {
        if (!forceRefresh) {
            List<MealDetail> cachedMeals = repository.getInspirationMeals();
            if (cachedMeals != null && !cachedMeals.isEmpty()) {
                view.hideLoading(); // <--- CRITICAL FIX: Hide shimmer when loading from cache
                view.showInspirationMeals(cachedMeals);
                return;
            }
        }

        view.showLoading();
        // Fetch 10 random meals concurrently
        disposable.add(Observable.range(0, 10)
                .flatMapSingle(i -> repository.getRandomMeal()
                        .subscribeOn(Schedulers.io()))
                .toList()
                .delay(2000, TimeUnit.MILLISECONDS) // UX: Ensure shimmer is seen briefly
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responses -> {
                    List<MealDetail> meals = new ArrayList<>();
                    for (MealResponse res : responses) {
                        if (res.getMeals() != null && !res.getMeals().isEmpty()) {
                            meals.add(res.getMeals().get(0));
                        }
                    }
                    if (!meals.isEmpty()) {
                        repository.saveInspirationMeals(meals);
                    }
                    view.hideLoading();
                    view.showInspirationMeals(meals);
                }, error -> {
                    view.hideLoading();
                    view.showError("Failed to load inspiration");
                }));
    }

    @Override
    public void onInspirationMealClicked(MealDetail meal) {
        if (meal != null) {
            android.util.Log.d("HomePresenter", "Navigating to meal: " + meal.getName());
            view.navigateToMealDetails(meal);
        }
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}