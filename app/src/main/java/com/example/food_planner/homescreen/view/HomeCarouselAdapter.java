package com.example.food_planner.homescreen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;

import java.util.ArrayList;
import java.util.List;

public class HomeCarouselAdapter extends RecyclerView.Adapter<HomeCarouselAdapter.ViewHolder> {

    private List<MealDetail> meals = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMealClick(MealDetail meal);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<MealDetail> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    public int getActualItemCount() {
        return meals.size();
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealDetail meal = meals.get(position);

        holder.tvName.setText(meal.getName());
        holder.tvCategory.setText(meal.getCategory());

        Glide.with(holder.itemView.getContext())
                .load(meal.getThumbUrl())
                .transform(new CenterCrop())
                .placeholder(R.drawable.ic_restaurant)
                .into(holder.ivThumb);

        // Fix: Set listener on the CardView because it is clickable in XML and consumes
        // events
        holder.cardView.setOnClickListener(v -> {
            android.util.Log.d("HomeCarouselAdapter", "Item clicked: " + meal.getName());
            if (listener != null) {
                listener.onMealClick(meal);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvCategory;
        View cardView; // Use View or MaterialCardView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivCarouselThumb);
            tvName = itemView.findViewById(R.id.tvCarouselName);
            tvCategory = itemView.findViewById(R.id.tvCarouselCategory);
            cardView = itemView.findViewById(R.id.cardCarousel);
        }
    }
}