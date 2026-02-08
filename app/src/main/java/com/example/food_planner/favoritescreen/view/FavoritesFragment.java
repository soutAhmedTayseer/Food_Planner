package com.example.food_planner.favoritescreen.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.favoritescreen.presenter.FavoritesPresenter;
import com.example.food_planner.favoritescreen.presenter.FavoritesPresenterImpl;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoritesView {

    private RecyclerView rvFavorites;
    private TextView tvEmptyState;
    private View guestOverlay;

    private FavoritesAdapter adapter;
    private FavoritesPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        // Initialize Presenter
        presenter = new FavoritesPresenterImpl(
                this,
                MealRepository.getInstance(requireContext()),
                new SharedPrefManager(requireContext())
        );

        presenter.checkMode();
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        guestOverlay = view.findViewById(R.id.guestOverlay);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter();
        rvFavorites.setAdapter(adapter);

        adapter.setOnFavItemClickListener(meal -> presenter.onMealClick(meal));
    }

    // --- View Interface Implementation ---

    @Override
    public void showGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.VISIBLE);
            guestOverlay.setOnClickListener(v -> showGuestLoginDialog());
        }
        showGuestLoginDialog();
    }

    private void showGuestLoginDialog() {
        AlertUtil.showLoginRequiredDialog(requireContext(), () -> presenter.onGuestLoginClick());
    }

    @Override
    public void hideGuestMode() {
        if (guestOverlay != null) {
            guestOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void showFavorites(List<MealDetail> meals) {
        rvFavorites.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        adapter.setList(meals);
    }

    @Override
    public void showEmptyState() {
        rvFavorites.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        adapter.setList(new ArrayList<>());
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void navigateToDetails(MealDetail meal) {
        if (getView() != null) {
            FavoritesFragmentDirections.ActionFavoritesToMealDetails action =
                    FavoritesFragmentDirections.actionFavoritesToMealDetails(meal);
            Navigation.findNavController(getView()).navigate(action);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}