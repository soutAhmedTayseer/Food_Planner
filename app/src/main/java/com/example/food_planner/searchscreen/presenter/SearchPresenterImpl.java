package com.example.food_planner.searchscreen.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.model.Meal;
import com.example.food_planner.model.MealItem;
import com.example.food_planner.searchscreen.view.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchPresenterImpl implements SearchPresenter {

    private final SearchView view;
    private final MealRepository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private List<MealItem> originalList = new ArrayList<>();

    public SearchPresenterImpl(SearchView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void searchByType(String type) {
        Single<Meal> apiCall;
        switch (type) {
            case "a": apiCall = repository.getAreas(); break;
            case "i": apiCall = repository.getIngredients(); break;
            default: apiCall = repository.getCategories(); break;
        }
        fetchData(apiCall);
    }

    private void fetchData(Single<Meal> apiCall) {
        view.showLoading();
        disposable.add(apiCall
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> view.hideLoading()) // Ensures Lottie stops
                .subscribe(response -> {
                    if (response.getItems() != null && !response.getItems().isEmpty()) {
                        originalList = response.getItems();
                        view.showData(originalList);
                    } else {
                        view.showError("No data found");
                    }
                }, error -> view.showError(error.getMessage())));
    }

    @Override
    public void searchLocalList(String query) {
        if (originalList == null || originalList.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) {
            view.showData(originalList);
        } else {
            List<MealItem> filtered = originalList.stream()
                    .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            view.showData(filtered);
        }
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}