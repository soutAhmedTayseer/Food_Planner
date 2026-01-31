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
import com.example.food_planner.R;
import com.example.food_planner.model.MealItem;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<MealItem> items = new ArrayList<>();

    // --- 1. Define the Listener Interface ---
    public interface OnItemClickListener {
        void onItemClick(String itemName, ImageView sharedImageView); // Add ImageView param
    }

    private OnItemClickListener listener;

    // --- 2. Create the Setter Method (THIS FIXES YOUR ERROR) ---
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<MealItem> newItems) {
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
        if (url != null) {
            Glide.with(holder.itemView.getContext()).load(url).placeholder(R.drawable.ic_launcher_foreground).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // --- 3. Trigger the Listener on Click ---
        holder.itemView.setOnClickListener(v -> {
            // Unique transition name for every item is REQUIRED
            ViewCompat.setTransitionName(holder.image, item.getName());

            if (listener != null) {
                listener.onItemClick(item.getName(), holder.image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
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