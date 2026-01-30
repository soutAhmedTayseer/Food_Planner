package com.example.food_planner.favoritescreen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.food_planner.R;
import com.example.food_planner.model.MealDetail;
import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<MealDetail> favList = new ArrayList<>();

    public void setList(List<MealDetail> meals) {
        this.favList = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We reuse the 'item_search_category' layout since it has an Image and Text
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealDetail meal = favList.get(position);
        holder.tvName.setText(meal.getName());

        Glide.with(holder.itemView.getContext())
                .load(meal.getThumbUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.ivThumb);
    }

    @Override
    public int getItemCount() { return favList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivThumb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match your item_search_category.xml
            tvName = itemView.findViewById(R.id.tvCategoryName);
            ivThumb = itemView.findViewById(R.id.ivCategoryThumb);
        }
    }
}