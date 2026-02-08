package com.example.food_planner.meal_details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_planner.R;
import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.ViewHolder> {

    private final List<String> steps;

    public InstructionsAdapter(List<String> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instruction_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Position is 0-indexed, steps are 1-indexed
        holder.tvStepNumber.setText(String.valueOf(position + 1));
        holder.tvInstruction.setText(steps.get(position).trim());

        // Hide the connector line for the last item
        if (position == steps.size() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvInstruction;
        View line;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvInstruction = itemView.findViewById(R.id.tvInstructionStep);
            line = itemView.findViewById(R.id.viewTimelineLine);
        }
    }
}