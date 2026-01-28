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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    // 1. Initialize with empty list to prevent NullPointerException immediately
    private List<MealItem> items = new ArrayList<>();

    public void setList(List<MealItem> newItems) {
        // 2. Safety check: If API returns null, just clear the list instead of crashing
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = newItems;
        }
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
        MealItem item = items.get(position);
        holder.name.setText(item.getName());

        String url = item.getThumbnailUrl();

        // 3. Only load with Glide if the URL exists
        if (url != null) {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground) // Show placeholder while loading
                    .error(R.drawable.ic_launcher_foreground)       // Show placeholder if error
                    .into(holder.image);
        } else {
            // For Countries (Areas) which return null for image, set a static icon
            holder.image.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return items.size(); // Safe now because items is initialized
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvCategoryName);
            image = itemView.findViewById(R.id.ivCategoryThumb);
        }
    }
}