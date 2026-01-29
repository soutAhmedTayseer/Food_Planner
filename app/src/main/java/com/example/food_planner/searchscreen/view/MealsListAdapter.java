package com.example.food_planner.searchscreen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealItem;

import java.util.ArrayList;
import java.util.List;

public class MealsListAdapter extends RecyclerView.Adapter<MealsListAdapter.ViewHolder> {

    private List<MealItem> meals = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMealClick(String mealId); // Pass ID to fetch full details later
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<MealItem> meals) {
        this.meals = (meals != null) ? meals : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reusing your existing item layout is fine
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealItem meal = meals.get(position);

        holder.tvName.setText(meal.getName());

        Glide.with(holder.itemView.getContext())
                .load(meal.getThumbnailUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivThumb);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // The API for lists usually returns "idMeal" in the object
                listener.onMealClick(meal.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            ivThumb = itemView.findViewById(R.id.ivCategoryThumb);
        }
    }
}