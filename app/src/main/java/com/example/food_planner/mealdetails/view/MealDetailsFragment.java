package com.example.food_planner.mealdetails.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.mealdetails.presenter.MealDetailsPresenter;
import com.example.food_planner.mealdetails.presenter.MealDetailsPresenterImpl;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealDetailsFragment extends Fragment implements MealDetailsView {

    private TextView tvName;
    private Chip chipArea, chipCategory;
    private ImageView ivThumb;
    private RecyclerView rvIngredients, rvInstructions;
    private YouTubePlayerView youTubePlayerView;
    private FloatingActionButton fabFavorite, fabCalendar;

    private MealDetailsPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // Initialize Presenter
        presenter = new MealDetailsPresenterImpl(this, MealRepository.getInstance(requireContext()));

        getLifecycle().addObserver(youTubePlayerView);

        if (getArguments() != null) {
            MealDetailsFragmentArgs args = MealDetailsFragmentArgs.fromBundle(getArguments());
            MealDetail mealDetail = args.getMealDetail();
            // Pass data to presenter
            presenter.setMealData(mealDetail);
        }

        setupListeners();
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvMealName);
        chipArea = view.findViewById(R.id.chipArea);
        chipCategory = view.findViewById(R.id.chipCategory);
        ivThumb = view.findViewById(R.id.ivDetailThumb);

        rvIngredients = view.findViewById(R.id.rvIngredients);
        rvInstructions = view.findViewById(R.id.rvInstructions);

        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        fabFavorite = view.findViewById(R.id.fabFavorite);
        fabCalendar = view.findViewById(R.id.fabCalendar);
    }

    private void setupListeners() {
        fabFavorite.setOnClickListener(v -> presenter.onFavoriteClick());
        fabCalendar.setOnClickListener(v -> showDatePicker());
    }

    // --- View Interface Implementations ---

    @Override
    public void showMealInfo(String name, String area, String category, String thumbUrl) {
        tvName.setText(name);
        chipArea.setText(area);
        chipCategory.setText(category);

        Glide.with(this)
                .load(thumbUrl)
                .placeholder(R.drawable.ic_restaurant)
                .into(ivThumb);
    }

    @Override
    public void showIngredients(List<MealDetail.Ingredient> ingredients) {
        IngredientsAdapter ingAdapter = new IngredientsAdapter(ingredients);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvIngredients.setAdapter(ingAdapter);
    }

    @Override
    public void showInstructions(List<String> steps) {
        InstructionsAdapter stepAdapter = new InstructionsAdapter(steps);
        rvInstructions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInstructions.setAdapter(stepAdapter);
    }

    @Override
    public void showVideo(String videoId) {
        youTubePlayerView.setVisibility(View.VISIBLE);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0);
            }
        });
    }

    @Override
    public void hideVideo() {
        youTubePlayerView.setVisibility(View.GONE);
    }

    @Override
    public void setFavoriteState(boolean isFavorite) {
        fabFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    @Override
    public void showMessage(String message) {
        SnackbarUtil.showSuccess(getView(), message);
    }

    @Override
    public void showError(String error) {
        SnackbarUtil.showError(getView(), error);
    }

    @Override
    public void showRemoveConfirmationDialog() {
        AlertUtil.showConfirmationDialog(
                requireContext(),
                getString(R.string.remove_from_favorites),
                getString(R.string.are_you_sure_you_want_to_remove_this_meal_from_your_favorites),
                () -> presenter.removeFromFavoritesConfirmed()
        );
    }

    // --- Local UI Utilities ---

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            presenter.addToPlan(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}