package com.example.harvesthub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.R;
import java.util.List;
import org.json.JSONObject;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private List<Goal> goals;
    private List<String> goalIds;
    private OnGoalActionListener actionListener;
    private int expandedPosition = -1;

    public GoalAdapter(List<Goal> goals, List<String> goalIds, OnGoalActionListener actionListener) {
        this.goals = goals;
        this.goalIds = goalIds;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        String goalId = goalIds.get(position);
        holder.goalName.setText(goal.name);
        holder.goalType.setText("Type: " + goal.type);
        holder.goalTargetQuantity.setText("Target: " + (goal.targetQuantity != null ? goal.targetQuantity : "N/A"));
        holder.goalDate.setText("Date: " + goal.targetDate);
        holder.goalPriority.setText("Priority: " + goal.priority);
        holder.goalRelatedCrop.setText("Crop: " + (goal.relatedCrop != null ? goal.relatedCrop : "N/A"));
        holder.buttonEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(goal, goalId);
        });
        holder.buttonDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(goal, goalId);
        });
        boolean isExpanded = position == expandedPosition;
        holder.setDetailsVisibility(isExpanded);
        holder.itemView.setOnClickListener(v -> {
            if (expandedPosition == position) {
                expandedPosition = -1;
            } else {
                int prev = expandedPosition;
                expandedPosition = position;
                if (prev >= 0) notifyItemChanged(prev);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void setGoals(List<Goal> newGoals, List<String> newGoalIds) {
        this.goals = newGoals;
        this.goalIds = newGoalIds;
        notifyDataSetChanged();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalName, goalType, goalTargetQuantity, goalDate, goalPriority, goalRelatedCrop;
        ImageButton buttonEdit, buttonDelete;
        View detailsContainer;
        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalName = itemView.findViewById(R.id.goal_name);
            goalType = itemView.findViewById(R.id.goal_type);
            goalTargetQuantity = itemView.findViewById(R.id.goal_target_quantity);
            goalDate = itemView.findViewById(R.id.goal_date);
            goalPriority = itemView.findViewById(R.id.goal_priority);
            goalRelatedCrop = itemView.findViewById(R.id.goal_related_crop);
            buttonEdit = itemView.findViewById(R.id.button_edit_goal);
            buttonDelete = itemView.findViewById(R.id.button_delete_goal);
            detailsContainer = itemView.findViewById(R.id.goal_details_container);
        }
        public void setDetailsVisibility(boolean visible) {
            if (detailsContainer != null) {
                detailsContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public interface OnGoalActionListener {
        void onEdit(Goal goal, String goalId);
        void onDelete(Goal goal, String goalId);
    }

    // Goal model for adapter
    public static class Goal {
        public String name;
        public String type;
        public String targetQuantity;
        public String targetDate;
        public String targetTime;
        public String priority;
        public String relatedCrop;
        public String status;
        public int progress;
        public Goal() {}
    }
} 