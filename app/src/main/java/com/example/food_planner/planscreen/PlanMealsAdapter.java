package com.example.food_planner.planscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.PlanMeal;

import java.util.ArrayList;
import java.util.List;

public class PlanMealsAdapter extends RecyclerView.Adapter<PlanMealsAdapter.ViewHolder> {

    private List<PlanMeal> meals = new ArrayList<>();
    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onMealClick(PlanMeal meal);

        void onDeleteClick(PlanMeal meal);
    }

    public PlanMealsAdapter(OnPlanClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<PlanMeal> newMeals) {
        this.meals = newMeals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_meal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanMeal meal = meals.get(position);
        holder.tvName.setText(meal.getMealName());
        holder.tvArea.setText(meal.getMealArea() + " • " + meal.getMealCategory());

        Glide.with(holder.itemView.getContext())
                .load(meal.getMealThumb())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivThumb);

        holder.itemView.setOnClickListener(v -> listener.onMealClick(meal));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(meal));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvArea;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivPlanThumb);
            tvName = itemView.findViewById(R.id.tvPlanName);
            tvArea = itemView.findViewById(R.id.tvPlanArea);
            btnDelete = itemView.findViewById(R.id.btnDeletePlan);
        }
    }
}