package com.example.harvesthub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.R;
import com.example.harvesthub.RemindersFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<RemindersFragment.Reminder> reminders;
    private OnReminderListener listener;
    private int expandedPosition = -1;

    public ReminderAdapter(List<RemindersFragment.Reminder> reminders, OnReminderListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        RemindersFragment.Reminder reminder = reminders.get(position);
        boolean isExpanded = position == expandedPosition;
        holder.bind(reminder, listener, isExpanded);
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
        return reminders.size();
    }

    public void setReminders(List<RemindersFragment.Reminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView goalName, activityType, cropName, date, notes;
        View detailsContainer;
        com.google.android.material.button.MaterialButton markDoneButton;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            goalName = itemView.findViewById(R.id.reminder_goal_name);
            activityType = itemView.findViewById(R.id.reminder_activity_type);
            cropName = itemView.findViewById(R.id.reminder_crop_name);
            date = itemView.findViewById(R.id.reminder_date);
            notes = itemView.findViewById(R.id.reminder_notes);
            detailsContainer = itemView.findViewById(R.id.reminder_details_container);
            markDoneButton = itemView.findViewById(R.id.button_mark_done);
        }

        public void bind(final RemindersFragment.Reminder reminder, final OnReminderListener listener, boolean isExpanded) {
            goalName.setText(reminder.goalName);
            activityType.setText("Activity: " + reminder.activityType);
            cropName.setText("Crop: " + (reminder.cropName != null ? reminder.cropName : "N/A"));
            date.setText("Date: " + dateFormat.format(new Date(reminder.dateMillis)));
            if (reminder.status != null && reminder.status.equals("done")) {
                markDoneButton.setEnabled(false);
                markDoneButton.setText("Completed");
            } else {
                markDoneButton.setEnabled(true);
                markDoneButton.setText("Mark as Done");
            }
            if (reminder.notes != null && !reminder.notes.trim().isEmpty()) {
                notes.setText("Notes: " + reminder.notes);
                notes.setVisibility(View.VISIBLE);
            } else {
                notes.setVisibility(View.GONE);
            }
            detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            markDoneButton.setOnClickListener(v -> listener.onMarkAsDone(reminder));
        }
    }

    public interface OnReminderListener {
        void onMarkAsDone(RemindersFragment.Reminder reminder);
    }
} 