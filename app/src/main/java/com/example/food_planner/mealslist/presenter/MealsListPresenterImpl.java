package com.example.food_planner.mealslist.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.mealslist.view.MealsListView;
import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealsListPresenterImpl implements MealsListPresenter {

    private final MealsListView view;
    private final MealRepository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private List<MealItem> originalList = new ArrayList<>();

    public MealsListPresenterImpl(MealsListView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void getMeals(String type, String queryName) {
        view.showLoading(true);
        Single<Meal> apiCall;

        if ("c".equals(type)) {
            apiCall = repository.filterByCategory(queryName);
        } else if ("a".equals(type)) {
            apiCall = repository.filterByArea(queryName);
        } else {
            apiCall = repository.filterByIngredient(queryName);
        }

        disposable.add(apiCall
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    view.showLoading(false);
                    if (response.getItems() != null) {
                        originalList = response.getItems();
                        view.showMeals(originalList);
                    }
                }, error -> {
                    view.showLoading(false);
                    view.showError("Error: " + error.getMessage());
                }));
    }

    @Override
    public void searchLocalList(String query) {
        if (originalList == null || originalList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            view.showMeals(new ArrayList<>(originalList));
        } else {
            List<MealItem> filtered = originalList.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            view.showMeals(filtered);
        }
    }

    @Override
    public void getMealDetails(String mealId) {
        // Fetch details silently (or minimal loading) to preserve old UX
        disposable.add(repository.getMealById(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        view.navigateToDetails(response.getMeals().get(0));
                    }
                }, error -> view.showError("Check internet connection")));
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}