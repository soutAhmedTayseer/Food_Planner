package com.example.food_planner.searchscreen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.food_planner.R;
import com.example.food_planner.model.MealItem;

import java.util.ArrayList;
import java.util.List;

public class MealsListAdapter extends RecyclerView.Adapter<MealsListAdapter.ViewHolder> {

    private List<MealItem> meals = new ArrayList<>();
    private OnItemClickListener listener;
    private int lastPosition = -1; // Track animation state

    public interface OnItemClickListener {
        void onMealClick(String mealId, ImageView sharedImageView);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealItem meal = meals.get(position);

        holder.tvName.setText(meal.getName());

        // Apply Glide with Rounded Corners for better UI
        Glide.with(holder.itemView.getContext())
                .load(meal.getThumbnailUrl())
                .transform(new CenterCrop(), new RoundedCorners(24))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivThumb);

        // Set Transition Name for Shared Element Animation
        ViewCompat.setTransitionName(holder.ivThumb, meal.getName());

        // Apply the entrance animation
        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the image view for transition
                listener.onMealClick(meal.getId(), holder.ivThumb);
            }
        });
    }

    /**
     * Staggered Slide-Up Animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            viewToAnimate.setAlpha(0f);
            viewToAnimate.setTranslationY(100f);

            viewToAnimate.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(position * 50L)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            lastPosition = position;
        }
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