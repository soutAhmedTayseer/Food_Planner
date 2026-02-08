package com.example.food_planner.mealdetails.presenter;

import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.mealdetails.view.MealDetailsView;
import com.example.food_planner.model.MealDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsPresenterImpl implements MealDetailsPresenter {

    private final MealDetailsView view;
    private final MealRepository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private MealDetail mealDetail;

    public MealDetailsPresenterImpl(MealDetailsView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void setMealData(MealDetail mealDetail) {
        this.mealDetail = mealDetail;
        if (mealDetail == null) return;

        // 1. Show Basic Info
        view.showMealInfo(mealDetail.getName(), mealDetail.getArea(), mealDetail.getCategory(), mealDetail.getThumbUrl());

        // 2. Show Ingredients
        view.showIngredients(mealDetail.getIngredients());

        // 3. Process and Show Instructions
        List<String> steps = new ArrayList<>();
        if (mealDetail.getInstructions() != null) {
            String[] rawSteps = mealDetail.getInstructions().split("(?<=\\.)\\s+|\\r\\n|\\n");
            for (String step : rawSteps) {
                if (!step.trim().isEmpty()) steps.add(step.trim());
            }
        }
        view.showInstructions(steps);

        // 4. Process and Show Video
        String videoId = extractVideoId(mealDetail.getYoutubeUrl());
        if (videoId != null && !videoId.isEmpty()) {
            view.showVideo(videoId);
        } else {
            view.hideVideo();
        }

        // 5. Check Initial Favorite Status
        checkFavoriteStatus();
    }

    @Override
    public void addToPlan(String date) {
        if (mealDetail == null) return;
        disposable.add(repository.addToPlan(mealDetail, date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> view.showMessage("Meal added to " + date),
                        error -> view.showError("Failed to add: " + error.getMessage())
                ));
    }

    @Override
    public void checkFavoriteStatus() {
        if (mealDetail == null) return;
        disposable.add(repository.isFavorite(mealDetail.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setFavoriteState,
                        error -> {} // Silent fail on check
                ));
    }

    @Override
    public void onFavoriteClick() {
        if (mealDetail == null) return;
        disposable.add(repository.isFavorite(mealDetail.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFav -> {
                    if (isFav) {
                        view.showRemoveConfirmationDialog();
                    } else {
                        addToFavorites();
                    }
                }, error -> view.showError("Operation failed")));
    }

    private void addToFavorites() {
        disposable.add(repository.addToFavorites(mealDetail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.setFavoriteState(true);
                    view.showMessage("Added to favorites");
                }, error -> view.showError("Failed to add")));
    }

    @Override
    public void removeFromFavoritesConfirmed() {
        disposable.add(repository.removeFromFavorites(mealDetail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.setFavoriteState(false);
                    view.showError("Removed from favorites"); // Keeping your original logic (using showError for removal msg)
                }, error -> view.showError("Failed to remove")));
    }

    private String extractVideoId(String url) {
        String videoId = null;
        if (url != null && url.trim().length() > 0) {
            String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\u200C\\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(url);
            if (matcher.find()) {
                videoId = matcher.group();
            }
        }
        return videoId;
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}