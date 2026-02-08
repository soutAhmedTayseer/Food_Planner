package com.example.food_planner.favoritescreen.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.favoritescreen.view.FavoritesView;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.SharedPrefManager;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FavoritesPresenterImpl implements FavoritesPresenter {

    private final FavoritesView view;
    private final MealRepository repository;
    private final SharedPrefManager sharedPrefManager;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public FavoritesPresenterImpl(FavoritesView view, MealRepository repository, SharedPrefManager sharedPrefManager) {
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
            getFavorites();
        }
    }

    @Override
    public void getFavorites() {
        disposable.add(repository.getStoredMeals()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> {
                            if (meals == null || meals.isEmpty()) {
                                view.showEmptyState();
                            } else {
                                view.showFavorites(meals);
                            }
                        },
                        error -> {
                            view.showError("Error loading favorites");
                            view.showEmptyState();
                        }
                ));
    }

    @Override
    public void onGuestLoginClick() {
        sharedPrefManager.logoutUser();
        view.navigateToLogin();
    }

    @Override
    public void onMealClick(MealDetail meal) {
        view.navigateToDetails(meal);
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}