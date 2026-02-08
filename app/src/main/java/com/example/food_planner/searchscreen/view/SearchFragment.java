package com.example.food_planner.searchscreen.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.model.MealItem;
import com.example.food_planner.searchscreen.presenter.SearchPresenter;
import com.example.food_planner.searchscreen.presenter.SearchPresenterImpl;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends Fragment implements SearchView, OnSearchItemClickListener {

    private static final String TAG = "SearchFragment";

    // UI
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
    private ChipGroup chipGroup;
    // Use fully qualified name to avoid import errors
    private androidx.appcompat.widget.SearchView searchView;
    private SearchAdapter adapter;

    // MVP
    private SearchPresenter presenter;

    // Logic
    private String currentType = "c";
    private final PublishSubject<String> searchSubject = PublishSubject.create();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        presenter = new SearchPresenterImpl(this, MealRepository.getInstance(requireContext()));

        setupRecyclerView();
        setupSearchLogic();
        setupListeners();

        loadData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rvSearch);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        chipGroup = view.findViewById(R.id.chipGroupSearch);
        searchView = view.findViewById(R.id.searchView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SearchAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (searchView != null) searchView.setQuery("", false);

            if (checkedId == R.id.chipCategory) currentType = "c";
            else if (checkedId == R.id.chipCountry) currentType = "a";
            else if (checkedId == R.id.chipIngredient) currentType = "i";

            loadData();
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSubject.onNext(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchSubject.onNext(newText);
                return true;
            }
        });
    }

    private void loadData() {
        if (!isNetworkAvailable()) {
            showError("No Internet Connection");
            hideLoading();
            return;
        }
        presenter.searchByType(currentType);
    }

    // --- MVP View Implementation (Restoring Old Animation Logic) ---

    @Override
    public void showLoading() {
        if (lottieLoading != null && recyclerView != null) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            // Fade out recycler
            recyclerView.animate().alpha(0f).setDuration(200).withEndAction(() -> recyclerView.setVisibility(View.GONE));
        }
    }

    @Override
    public void hideLoading() {
        if (lottieLoading != null && recyclerView != null) {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // Fade in recycler
            recyclerView.animate().alpha(1f).setDuration(300);
        }
    }

    @Override
    public void showData(List<MealItem> items) {
        adapter.setList(items);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void showError(String message) {
        if (getView() != null) SnackbarUtil.showError(getView(), message);
    }

    // --- Interaction ---

    @Override
    public void onItemClick(String itemName, ImageView sharedImageView) {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(sharedImageView, "shared_image")
                .build();

        // Ensure you have generated the directions
        SearchFragmentDirections.ActionSearchToList action =
                SearchFragmentDirections.actionSearchToList(currentType, itemName);
        Navigation.findNavController(requireView()).navigate(action, extras);
    }

    private void setupSearchLogic() {
        disposable.add(searchSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> presenter.searchLocalList(query),
                        error -> Log.e(TAG, "Error", error)));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) presenter.onDestroy();
        disposable.clear();
    }
}