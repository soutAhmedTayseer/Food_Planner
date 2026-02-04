package com.example.food_planner.meal_details.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.repository.MealRepository;
import com.example.food_planner.utils.AlertUtil; // Import AlertUtil
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsFragment extends Fragment {

    private TextView tvName, tvArea, tvInstructions;
    private ImageView ivThumb;
    private RecyclerView rvIngredients;
    private WebView webView;

    private FloatingActionButton fabFavorite;
    private FloatingActionButton fabCalendar;

    private MealDetail mealDetail;
    private MealRepository mealRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        mealRepository = new MealRepository(requireContext());

        if (getArguments() != null) {
            MealDetailsFragmentArgs args = MealDetailsFragmentArgs.fromBundle(getArguments());
            mealDetail = args.getMealDetail();

            if (mealDetail != null) {
                bindData();
                checkFavoriteStatus();
            }
        }
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvMealName);
        tvArea = view.findViewById(R.id.tvMealArea);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        ivThumb = view.findViewById(R.id.ivDetailThumb);
        rvIngredients = view.findViewById(R.id.rvIngredients);
        webView = view.findViewById(R.id.webViewVideo);

        fabFavorite = view.findViewById(R.id.fabFavorite);
        fabCalendar = view.findViewById(R.id.fabCalendar);
    }

    private void bindData() {
        tvName.setText(mealDetail.getName());
        tvArea.setText(mealDetail.getArea() + " | " + mealDetail.getCategory());
        tvInstructions.setText(mealDetail.getInstructions());

        Glide.with(this).load(mealDetail.getThumbUrl()).placeholder(R.drawable.ic_launcher_background).into(ivThumb);

        IngredientsAdapter adapter = new IngredientsAdapter(mealDetail.getIngredients());
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvIngredients.setAdapter(adapter);

        loadVideo(mealDetail.getYoutubeUrl());

        setupFavoriteClick();

        if (fabCalendar != null) {
            fabCalendar.setOnClickListener(v -> showDatePicker());
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            saveToPlan(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void saveToPlan(String date) {
        disposable.add(mealRepository.addToPlan(mealDetail, date).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> SnackbarUtil.showSuccess(getView(), "Meal added to " + date), error -> SnackbarUtil.showError(getView(), "Failed to add: " + error.getMessage())));
    }

    private void checkFavoriteStatus() {
        disposable.add(mealRepository.isFavorite(mealDetail.getId()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(isFav -> {
            if (isFav) {
                fabFavorite.setImageResource(R.drawable.ic_favorite);
            } else {
                fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            }
        }, error -> {
        }));
    }

    private void setupFavoriteClick() {
        fabFavorite.setOnClickListener(v -> {
            disposable.add(mealRepository.isFavorite(mealDetail.getId()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(isFav -> {
                if (isFav) {
                    // SHOW ALERT before removing
                    AlertUtil.showConfirmationDialog(requireContext(), getString(R.string.remove_from_favorites), getString(R.string.are_you_sure_you_want_to_remove_this_meal_from_your_favorites), this::removeFromFavorites // Callback to actual remove logic
                    );
                } else {
                    // Add directly
                    addToFavorites();
                }
            }, error -> Toast.makeText(requireContext(), "Operation failed", Toast.LENGTH_SHORT).show()));
        });
    }

    private void addToFavorites() {
        disposable.add(mealRepository.addToFavorites(mealDetail).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
            SnackbarUtil.showSuccess(getView(), getString(R.string.added_to_favorites));
        }, error -> SnackbarUtil.showError(getView(), "Failed to add")));
    }

    private void removeFromFavorites() {
        disposable.add(mealRepository.removeFromFavorites(mealDetail).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            SnackbarUtil.showError(getView(), getString(R.string.removed_from_favorites));
        }, error -> SnackbarUtil.showError(getView(), "Failed to remove")));
    }

    private void loadVideo(String url) {
        if (url != null && !url.isEmpty() && url.contains("v=")) {
            String videoId = url.split("v=")[1];
            String embedUrl = "https://www.youtube.com/embed/" + videoId;
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(embedUrl);
        } else {
            webView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}