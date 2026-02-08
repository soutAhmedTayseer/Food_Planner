package com.example.food_planner.mealslist.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.food_planner.R;
import com.example.food_planner.data.repository.MealRepository;
import com.example.food_planner.mealslist.presenter.MealsListPresenter;
import com.example.food_planner.mealslist.presenter.MealsListPresenterImpl;
import com.example.food_planner.model.MealDetail;
import com.example.food_planner.model.MealItem;

import java.util.List;

public class MealsListFragment extends Fragment implements MealsListView {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoading;
    private SearchView searchView;
    private TextView tvTitle;

    private MealsListAdapter adapter;
    private MealsListPresenter presenter;
    private ImageView pendingTransitionView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meals_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();

        presenter = new MealsListPresenterImpl(this, MealRepository.getInstance(requireContext()));

        if (getArguments() != null) {
            MealsListFragmentArgs args = MealsListFragmentArgs.fromBundle(getArguments());
            String type = args.getFilterType();
            String name = args.getFilterName();

            if (name != null) tvTitle.setText(name);
            presenter.getMeals(type, name);
        }

        setupSearchListener();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rvMealsList);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        searchView = view.findViewById(R.id.searchView);
        tvTitle = view.findViewById(R.id.tvListTitle);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new MealsListAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((mealId, sharedImageView) -> {
            this.pendingTransitionView = sharedImageView;
            presenter.getMealDetails(mealId);
        });
    }

    private void setupSearchListener() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    presenter.searchLocalList(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    presenter.searchLocalList(newText);
                    return true;
                }
            });
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        if (lottieLoading == null || recyclerView == null) return;

        if (isLoading) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            recyclerView.animate().alpha(0f).setDuration(200).withEndAction(() -> recyclerView.setVisibility(View.GONE));
        } else {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.animate().alpha(1f).setDuration(300);
        }
    }

    @Override
    public void showMeals(List<MealItem> meals) {
        adapter.setList(meals);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void navigateToDetails(MealDetail meal) {
        if (getView() == null) return;

        FragmentNavigator.Extras extras = null;
        if (pendingTransitionView != null) {
            extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(pendingTransitionView, "shared_image")
                    .build();
        }

        MealsListFragmentDirections.ActionListToDetails action =
                MealsListFragmentDirections.actionListToDetails(meal);

        if (extras != null) {
            Navigation.findNavController(requireView()).navigate(action, extras);
        } else {
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}