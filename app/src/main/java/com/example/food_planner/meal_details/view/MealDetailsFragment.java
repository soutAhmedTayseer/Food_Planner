package com.example.food_planner.meal_details.view;

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
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsFragment extends Fragment {

    private TextView tvName, tvArea, tvInstructions;
    private ImageView ivThumb;
    private RecyclerView rvIngredients;
    private WebView webView;
    private FloatingActionButton fabFavorite;

    private MealDetail mealDetail;
    private MealRepository mealRepository; // Replaces SharedPrefManager
    private final CompositeDisposable disposable = new CompositeDisposable(); // To manage subscriptions

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // 1. Initialize Repository
        mealRepository = new MealRepository(requireContext());

        if (getArguments() != null) {
            MealDetailsFragmentArgs args = MealDetailsFragmentArgs.fromBundle(getArguments());
            mealDetail = args.getMealDetail();

            if (mealDetail != null) {
                bindData();
                // 2. Check initial favorite state from DB
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
    }

    private void bindData() {
        tvName.setText(mealDetail.getName());
        tvArea.setText(mealDetail.getArea() + " | " + mealDetail.getCategory());
        tvInstructions.setText(mealDetail.getInstructions());

        Glide.with(this)
                .load(mealDetail.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivThumb);

        IngredientsAdapter adapter = new IngredientsAdapter(mealDetail.getIngredients());
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvIngredients.setAdapter(adapter);

        loadVideo(mealDetail.getYoutubeUrl());

        // 3. Setup Click Listener (Your Snippet)
        setupFavoriteClick();
    }

    private void checkFavoriteStatus() {
        disposable.add(
                mealRepository.isFavorite(mealDetail.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isFav -> {
                            if (isFav) {
                                fabFavorite.setImageResource(R.drawable.ic_favorite);
                            } else {
                                fabFavorite.setImageResource(R.drawable.ic_favorite_border);
                            }
                        }, error -> Toast.makeText(requireContext(), "Error checking favorites", Toast.LENGTH_SHORT).show())
        );
    }

    private void setupFavoriteClick() {
        fabFavorite.setOnClickListener(v -> {
            disposable.add(
                    mealRepository.isFavorite(mealDetail.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(isFav -> {
                                if (isFav) {
                                    // Remove from Room
                                    mealRepository.removeFromFavorites(mealDetail).subscribe();
                                    fabFavorite.setImageResource(R.drawable.ic_favorite_border);
                                    ViewUtils.showError(getView(), getString(R.string.removed_from_favorites));
                                } else {
                                    // Add to Room
                                    mealRepository.addToFavorites(mealDetail).subscribe();
                                    fabFavorite.setImageResource(R.drawable.ic_favorite);
                                    ViewUtils.showSuccess(getView(), getString(R.string.added_to_favorites));
                                }
                            }, error -> Toast.makeText(requireContext(), "Operation failed", Toast.LENGTH_SHORT).show())
            );
        });
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
        // Clear subscriptions to prevent memory leaks
        disposable.clear();
    }
}