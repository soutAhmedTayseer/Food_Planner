package com.example.food_planner.meal_details;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsFragment extends Fragment {

    private TextView tvName;
    private Chip chipArea, chipCategory;
    private ImageView ivThumb;
    private RecyclerView rvIngredients, rvInstructions;
    private YouTubePlayerView youTubePlayerView;
    private FloatingActionButton fabFavorite, fabCalendar;

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
        getLifecycle().addObserver(youTubePlayerView);

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
        chipArea = view.findViewById(R.id.chipArea);
        chipCategory = view.findViewById(R.id.chipCategory);
        ivThumb = view.findViewById(R.id.ivDetailThumb);

        rvIngredients = view.findViewById(R.id.rvIngredients);
        rvInstructions = view.findViewById(R.id.rvInstructions);

        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        fabFavorite = view.findViewById(R.id.fabFavorite);
        fabCalendar = view.findViewById(R.id.fabCalendar);
    }

    private void bindData() {
        tvName.setText(mealDetail.getName());
        chipArea.setText(mealDetail.getArea());
        chipCategory.setText(mealDetail.getCategory());

        Glide.with(this)
                .load(mealDetail.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivThumb);

        // 1. Setup Ingredients (Horizontal)
        IngredientsAdapter ingAdapter = new IngredientsAdapter(mealDetail.getIngredients());
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvIngredients.setAdapter(ingAdapter);

        // 2. Setup Instructions (Vertical Timeline)
        // Split instructions by newlines to create steps
        List<String> steps = new ArrayList<>();
        if (mealDetail.getInstructions() != null) {
            // Split by period followed by newline, or just newlines
            String[] rawSteps = mealDetail.getInstructions().split("(?<=\\.)\\s+|\\r\\n|\\n");
            for(String step : rawSteps) {
                if(!step.trim().isEmpty()) steps.add(step.trim());
            }
        }
        InstructionsAdapter stepAdapter = new InstructionsAdapter(steps);
        rvInstructions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInstructions.setAdapter(stepAdapter);

        // 3. Load Video
        loadVideo(mealDetail.getYoutubeUrl());

        setupFavoriteClick();
        fabCalendar.setOnClickListener(v -> showDatePicker());
    }

    private void loadVideo(String url) {
        String videoId = extractVideoId(url);
        if (videoId != null && !videoId.isEmpty()) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoId, 0);
                }
            });
        } else {
            youTubePlayerView.setVisibility(View.GONE);
        }
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

    // --- LOGIC (Calendar/Favorites) ---

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
        disposable.add(mealRepository.addToPlan(mealDetail, date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> SnackbarUtil.showSuccess(getView(), "Meal added to " + date),
                        error -> SnackbarUtil.showError(getView(), "Failed to add: " + error.getMessage())
                ));
    }

    private void checkFavoriteStatus() {
        disposable.add(mealRepository.isFavorite(mealDetail.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFav -> {
                    fabFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                }, error -> {}));
    }

    private void setupFavoriteClick() {
        fabFavorite.setOnClickListener(v -> {
            disposable.add(mealRepository.isFavorite(mealDetail.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isFav -> {
                        if (isFav) {
                            AlertUtil.showConfirmationDialog(requireContext(), getString(R.string.remove_from_favorites), getString(R.string.are_you_sure_you_want_to_remove_this_meal_from_your_favorites), this::removeFromFavorites);
                        } else {
                            addToFavorites();
                        }
                    }, error -> Toast.makeText(requireContext(), "Operation failed", Toast.LENGTH_SHORT).show()));
        });
    }

    private void addToFavorites() {
        disposable.add(mealRepository.addToFavorites(mealDetail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    fabFavorite.setImageResource(R.drawable.ic_favorite);
                    SnackbarUtil.showSuccess(getView(), getString(R.string.added_to_favorites));
                }, error -> SnackbarUtil.showError(getView(), "Failed to add")));
    }

    private void removeFromFavorites() {
        disposable.add(mealRepository.removeFromFavorites(mealDetail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    fabFavorite.setImageResource(R.drawable.ic_favorite_border);
                    SnackbarUtil.showError(getView(), getString(R.string.removed_from_favorites));
                }, error -> SnackbarUtil.showError(getView(), "Failed to remove")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}