package com.example.harvesthub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.R;
import com.example.harvesthub.models.Activity;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
    private List<Activity> activities;
    private SimpleDateFormat dateFormat;

    public RecentActivityAdapter(List<Activity> activities) {
        this.activities = activities;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        
        // Set activity type icon
        int iconResId = getActivityTypeIcon(activity.getType());
        holder.activityTypeIcon.setImageResource(iconResId);
        
        holder.activityType.setText(activity.getType());
        holder.activityDate.setText(dateFormat.format(activity.getDate()));
        holder.activityDescription.setText(activity.getDescription());
        
        if (activity.getNotes() != null && !activity.getNotes().isEmpty()) {
            holder.activityNotes.setVisibility(View.VISIBLE);
            holder.activityNotes.setText(activity.getNotes());
        } else {
            holder.activityNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    private int getActivityTypeIcon(String activityType) {
        switch (activityType.toLowerCase()) {
            case "planting":
                return R.drawable.ic_planting;
            case "watering":
                return R.drawable.ic_watering;
            case "fertilizing":
                return R.drawable.ic_fertilizing;
            case "harvesting":
                return R.drawable.ic_harvesting;
            case "pest control":
                return R.drawable.ic_pest_control;
            case "pruning":
                return R.drawable.ic_pruning;
            default:
                return R.drawable.ic_activity;
        }
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ImageView activityTypeIcon;
        TextView activityType;
        TextView activityDate;
        TextView activityDescription;
        TextView activityNotes;

        ActivityViewHolder(View itemView) {
            super(itemView);
            activityTypeIcon = itemView.findViewById(R.id.activity_type_icon);
            activityType = itemView.findViewById(R.id.activity_type);
            activityDate = itemView.findViewById(R.id.activity_date);
            activityDescription = itemView.findViewById(R.id.activity_description);
            activityNotes = itemView.findViewById(R.id.activity_notes);
        }
    }
} 