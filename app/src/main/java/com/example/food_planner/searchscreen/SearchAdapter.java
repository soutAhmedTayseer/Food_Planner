package com.example.food_planner.searchscreen;

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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<MealItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private int lastPosition = -1; // For animation state

    public interface OnItemClickListener {
        void onItemClick(String itemName, ImageView sharedImageView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<MealItem> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<>();
        // Reset animation when list changes significantly (optional, depends on preference)
        // lastPosition = -1;
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
        holder.tvName.setText(item.getName());

        String url = item.getThumbnailUrl();

        // Using Glide with transformations for better card visuals
        Glide.with(holder.itemView.getContext())
                .load(url)
                .transform(new CenterCrop(), new RoundedCorners(24)) // 24px radius matches modern UI
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivThumb);

        // --- Shared Element Transition Name ---
        ViewCompat.setTransitionName(holder.ivThumb, item.getName());

        // --- Satisfying Entrance Animation ---
        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item.getName(), holder.ivThumb);
            }
        });
    }

    /**
     * Applies a slide-in and fade-in animation to the view.
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            // We create the animation programmatically to ensure it works without extra XML files
            viewToAnimate.setAlpha(0f);
            viewToAnimate.setTranslationY(100f); // Start slightly below

            viewToAnimate.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400) // Duration of animation
                    .setStartDelay(position * 50L) // Staggered effect (cascading)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            ivThumb = itemView.findViewById(R.id.ivCategoryThumb);
        }
    }
}