package com.example.food_planner.homescreen.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.homescreen.presenter.HomePresenter;
import com.example.food_planner.homescreen.presenter.HomePresenterImpl;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.utils.SnackbarUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;

import java.util.List;

public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter presenter;

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private NestedScrollView contentLayout;
    private ImageView ivDailyMeal;
    private TextView tvDailyMealName;
    private MaterialCardView cardMealOfDay;
    private View layoutCardFront, layoutCardBack;
    private RecyclerView rvCarousel;

    // Adapters & Helpers
    private HomeCarouselAdapter carouselAdapter;
    private CarouselSnapHelper snapHelper;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private static final long SCROLL_DELAY = 4000;
    private final Runnable sliderRunnable = this::autoScrollCarousel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupCarousel();

        // Initialize Presenter
        presenter = new HomePresenterImpl(this, MealRepository.getInstance(requireContext()));

        // Start Data Loading
        presenter.getDailyMeal();
        presenter.getInspirationMeals();

        setupSwipeRefresh();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        ivDailyMeal = view.findViewById(R.id.ivDailyMeal);
        tvDailyMealName = view.findViewById(R.id.tvDailyMealName);
        cardMealOfDay = view.findViewById(R.id.cardMealOfDay);
        layoutCardFront = view.findViewById(R.id.layout_card_front);
        layoutCardBack = view.findViewById(R.id.layout_card_back);
        rvCarousel = view.findViewById(R.id.rvCarousel);
    }

    private void setupCarousel() {
        carouselAdapter = new HomeCarouselAdapter();
        rvCarousel.setLayoutManager(new CarouselLayoutManager(new HeroCarouselStrategy()));

        snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(rvCarousel);

        rvCarousel.setAdapter(carouselAdapter);
        rvCarousel.setClipChildren(false);
        rvCarousel.setClipToPadding(false);

        carouselAdapter.setOnItemClickListener(this::navigateToDetails);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isNetworkAvailable()) {
                presenter.getInspirationMeals();
            } else {
                showNetworkError();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // --- MVP View Implementation ---

    @Override
    public void showLoading() {
        if (!swipeRefreshLayout.isRefreshing()) {
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showDailyMeal(MealDetail meal) {
        layoutCardFront.setVisibility(View.GONE);
        layoutCardBack.setVisibility(View.VISIBLE);
        bindDailyMealData(meal);
    }

    @Override
    public void showMysteryCard() {
        layoutCardFront.setVisibility(View.VISIBLE);
        layoutCardBack.setVisibility(View.GONE);
        cardMealOfDay.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                cardMealOfDay.setClickable(false);
                presenter.requestNewDailyMeal();
            } else {
                showNetworkError();
            }
        });
    }

    @Override
    public void animateFlipToMeal(MealDetail meal) {
        bindDailyMealData(meal);
        final float scale = requireContext().getResources().getDisplayMetrics().density;
        cardMealOfDay.setCameraDistance(8000 * scale);

        layoutCardFront.animate().withLayer().rotationY(90).setDuration(300).withEndAction(() -> {
            layoutCardFront.setVisibility(View.GONE);
            layoutCardBack.setVisibility(View.VISIBLE);
            layoutCardBack.setRotationY(-90);
            layoutCardBack.animate().withLayer().rotationY(0).setDuration(300).withEndAction(() -> {
                cardMealOfDay.setClickable(true);
            }).start();
        }).start();
    }

    @Override
    public void showInspirationMeals(List<MealDetail> meals) {
        carouselAdapter.setList(meals);
        if (!meals.isEmpty()) startAutoScroll();
    }

    @Override
    public void showError(String message) {
        SnackbarUtil.showError(getView(), message);
        cardMealOfDay.setClickable(true);
    }

    @Override
    public void showNetworkError() {
        SnackbarUtil.showError(getView(), "No Internet Connection");
    }

    // --- Helpers ---

    private void bindDailyMealData(MealDetail meal) {
        tvDailyMealName.setText(meal.getName());
        Glide.with(this).load(meal.getThumbUrl())
                .placeholder(R.drawable.ic_restaurant)
                .into(ivDailyMeal);
        cardMealOfDay.setOnClickListener(v -> navigateToDetails(meal));
    }

    private void navigateToDetails(MealDetail meal) {
        if (meal != null) {
            HomeFragmentDirections.ActionHomeToMealDetails action =
                    HomeFragmentDirections.actionHomeToMealDetails(meal);
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private void autoScrollCarousel() {
        if (rvCarousel == null || carouselAdapter == null || carouselAdapter.getActualItemCount() == 0) return;

        RecyclerView.LayoutManager layoutManager = rvCarousel.getLayoutManager();
        if (layoutManager != null && snapHelper != null) {
            View snapView = snapHelper.findSnapView(layoutManager);
            int currentPosition = (snapView != null) ? layoutManager.getPosition(snapView) : 0;
            int nextPosition = currentPosition + 1;

            if (nextPosition >= carouselAdapter.getItemCount()) {
                rvCarousel.scrollToPosition(0);
            } else {
                rvCarousel.smoothScrollToPosition(nextPosition);
            }
        }
        sliderHandler.postDelayed(sliderRunnable, SCROLL_DELAY);
    }

    private void startAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, SCROLL_DELAY);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselAdapter != null && carouselAdapter.getItemCount() > 0) startAutoScroll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}