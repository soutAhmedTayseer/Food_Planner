package com.example.food_planner.homescreen.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.network.FoodApi;
import com.example.food_planner.network.NetworkClient;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private NestedScrollView contentLayout;
    private ImageView ivDailyMeal;
    private TextView tvDailyMealName;
    private MaterialCardView cardMealOfDay;
    private View layoutCardFront;
    private View layoutCardBack;
    private RecyclerView rvCarousel;

    private FoodApi foodApi;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private MealDetail currentMeal;
    private SharedPrefManager sharedPrefManager;
    private HomeCarouselAdapter carouselAdapter;
    private CarouselSnapHelper snapHelper;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private static final long SCROLL_DELAY = 4000;

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (rvCarousel != null && carouselAdapter != null && carouselAdapter.getItemCount() > 0) {
                RecyclerView.LayoutManager layoutManager = rvCarousel.getLayoutManager();
                if (layoutManager != null && snapHelper != null) {
                    View snapView = snapHelper.findSnapView(layoutManager);
                    int currentPosition = 0;
                    if (snapView != null) {
                        currentPosition = layoutManager.getPosition(snapView);
                    }

                    int nextPosition = currentPosition + 1;

                    // FIX: Loop back to 0 if we reached the end
                    if (nextPosition >= carouselAdapter.getItemCount()) {
                        nextPosition = 0;
                        rvCarousel.scrollToPosition(nextPosition); // Instant jump to start
                    } else {
                        rvCarousel.smoothScrollToPosition(nextPosition); // Smooth scroll to next
                    }
                }
                sliderHandler.postDelayed(this, SCROLL_DELAY);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupApi();
        setupCarousel();
        loadData(true);
        swipeRefreshLayout.setOnRefreshListener(() -> loadData(false));
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
        sharedPrefManager = new SharedPrefManager(requireContext());
    }

    private void setupApi() {
        foodApi = NetworkClient.getRetrofitInstance().create(FoodApi.class);
    }

    private void setupCarousel() {
        carouselAdapter = new HomeCarouselAdapter();
        // Use Hero Strategy for the center-zoom effect
        CarouselLayoutManager layoutManager = new CarouselLayoutManager(new HeroCarouselStrategy());
        rvCarousel.setLayoutManager(layoutManager);

        snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(rvCarousel);

        rvCarousel.setAdapter(carouselAdapter);
        rvCarousel.setClipChildren(false);
        rvCarousel.setClipToPadding(false);

        carouselAdapter.setOnItemClickListener(this::navigateToDetails);
    }

    private void loadData(boolean isInitialLoad) {
        if (!isNetworkAvailable()) {
            swipeRefreshLayout.setRefreshing(false);
            if (isInitialLoad && carouselAdapter.getItemCount() == 0) {
                showContent(true);
            }
            SnackbarUtil.showError(getView(), "No Internet Connection");
            checkDailyMealStatus();
            return;
        }

        if (isInitialLoad) showShimmer(true);
        disposable.clear();
        checkDailyMealStatus();

        disposable.add(Observable.range(0, 10)
                .flatMapSingle(i -> foodApi.getRandomMeal().subscribeOn(Schedulers.io()))
                .toList()
                .delay(isInitialLoad ? 1500 : 0, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responses -> {
                    List<MealDetail> carouselMeals = new ArrayList<>();
                    for (com.example.food_planner.model.MealResponse response : responses) {
                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                            carouselMeals.add(response.getMeals().get(0));
                        }
                    }
                    carouselAdapter.setList(carouselMeals);
                    if (!carouselMeals.isEmpty()) startAutoScroll();

                    showShimmer(false);
                    swipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    showShimmer(false);
                    swipeRefreshLayout.setRefreshing(false);
                    SnackbarUtil.showError(getView(), "Failed to refresh data");
                }));
    }

    private void checkDailyMealStatus() {
        MealDetail savedMeal = sharedPrefManager.getValidDailyMeal();
        if (savedMeal != null) {
            currentMeal = savedMeal;
            updateDailyMealUI();
        } else {
            showMysteryState();
        }
    }

    private void showMysteryState() {
        layoutCardFront.setVisibility(View.VISIBLE);
        layoutCardBack.setVisibility(View.GONE);
        cardMealOfDay.setOnClickListener(v -> fetchAndFlip());
    }

    private void fetchAndFlip() {
        if (!isNetworkAvailable()) {
            SnackbarUtil.showError(getView(), "No Internet Connection");
            return;
        }
        cardMealOfDay.setClickable(false);
        disposable.add(foodApi.getRandomMeal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        currentMeal = response.getMeals().get(0);
                        sharedPrefManager.saveDailyMeal(currentMeal);
                        updateDailyMealUI();
                        performFlipAnimation();
                    } else {
                        cardMealOfDay.setClickable(true);
                    }
                }, error -> {
                    SnackbarUtil.showError(getView(), "Failed to reveal meal");
                    cardMealOfDay.setClickable(true);
                }));
    }

    private void updateDailyMealUI() {
        tvDailyMealName.setText(currentMeal.getName());
        Glide.with(this).load(currentMeal.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivDailyMeal);
        layoutCardFront.setVisibility(View.GONE);
        layoutCardBack.setVisibility(View.VISIBLE);
        cardMealOfDay.setOnClickListener(v -> navigateToDetails(currentMeal));
    }

    private void showShimmer(boolean show) {
        if (show) {
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showContent(boolean show) {
        contentLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        shimmerFrameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void performFlipAnimation() {
        final float scale = requireContext().getResources().getDisplayMetrics().density;
        cardMealOfDay.setCameraDistance(8000 * scale);
        layoutCardFront.animate().withLayer().rotationY(90).setDuration(300).withEndAction(() -> {
            layoutCardFront.setVisibility(View.GONE);
            layoutCardBack.setVisibility(View.VISIBLE);
            layoutCardBack.setRotationY(-90);
            layoutCardBack.animate().withLayer().rotationY(0).setDuration(300).withEndAction(() -> {
                cardMealOfDay.setClickable(true);
                cardMealOfDay.setOnClickListener(v -> navigateToDetails(currentMeal));
            }).start();
        }).start();
    }

    private void navigateToDetails(MealDetail meal) {
        if (meal != null) {
            HomeFragmentDirections.ActionHomeToMealDetails action = HomeFragmentDirections.actionHomeToMealDetails(meal);
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private void startAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, SCROLL_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselAdapter != null && carouselAdapter.getItemCount() > 0) {
            startAutoScroll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}