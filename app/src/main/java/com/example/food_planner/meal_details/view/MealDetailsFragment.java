package com.example.food_planner.meal_details.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MealDetailsFragment extends Fragment {

    private TextView tvName, tvArea, tvInstructions;
    private ImageView ivThumb;
    private RecyclerView rvIngredients;
    private WebView webView;
    private MealDetail mealDetail;
    private FloatingActionButton fabFavorite; // Add this
    private SharedPrefManager sharedPrefManager; // Add this

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        sharedPrefManager = new SharedPrefManager(requireContext());

        if (getArguments() != null) {
            MealDetailsFragmentArgs args = MealDetailsFragmentArgs.fromBundle(getArguments());
            mealDetail = args.getMealDetail();

            if (mealDetail != null) {
                bindData();
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
        // Text Data
        tvName.setText(mealDetail.getName());
        tvArea.setText(mealDetail.getArea() + " | " + mealDetail.getCategory());
        tvInstructions.setText(mealDetail.getInstructions());

        // Image Loading
        Glide.with(this)
                .load(mealDetail.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivThumb);

        // Ingredients List
        IngredientsAdapter adapter = new IngredientsAdapter(mealDetail.getIngredients());
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvIngredients.setAdapter(adapter);

        // YouTube Video
        loadVideo(mealDetail.getYoutubeUrl());

        fabFavorite.setOnClickListener(v -> {
            if (mealDetail != null) {
                sharedPrefManager.addMealToFavorites(mealDetail);
                ViewUtils.showSuccess(getView(), "Added to Favorites!");
            }
        });
    }


    private void loadVideo(String url) {
        if (url != null && !url.isEmpty() && url.contains("v=")) {
            String videoId = url.split("v=")[1]; // Get ID after "v="
            String embedUrl = "https://www.youtube.com/embed/" + videoId;

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(embedUrl);
        } else {
            webView.setVisibility(View.GONE);
        }
    }
}