package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.adapters.GoalAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;
import android.os.Handler;

public class SetGoalsFragment extends Fragment {
    private RecyclerView goalsRecyclerView;
    private GoalAdapter goalAdapter;
    private List<GoalAdapter.Goal> goalList;
    private List<String> goalIds;
    private FloatingActionButton fabAddGoal;
    private String editingGoalId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_goals, container, false);
        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view);
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        goalList = new ArrayList<>();
        goalIds = new ArrayList<>();
        goalAdapter = new GoalAdapter(goalList, goalIds, new GoalAdapter.OnGoalActionListener() {
            @Override
            public void onEdit(GoalAdapter.Goal goal, String goalId) {
                showGoalDialog(goal, goalId);
            }
            @Override
            public void onDelete(GoalAdapter.Goal goal, String goalId) {
                new AlertDialog.Builder(getContext())
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteGoalFromFirebase(goalId))
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        goalsRecyclerView.setAdapter(goalAdapter);
        loadGoalsFromFirebase();
        fabAddGoal = view.findViewById(R.id.fab_add_goal);
        fabAddGoal.setOnClickListener(v -> showGoalDialog(null, null));
        return view;
    }

    private void showGoalDialog(@Nullable GoalAdapter.Goal goal, @Nullable String goalId) {
        AddGoalDialogFragment dialog = new AddGoalDialogFragment();
        if (goal != null) {
            Bundle prefill = new Bundle();
            prefill.putString("name", goal.name);
            prefill.putString("type", goal.type);
            prefill.putString("priority", goal.priority);
            prefill.putString("targetDate", goal.targetDate);
            prefill.putString("targetQuantity", goal.targetQuantity);
            prefill.putString("relatedCrop", goal.relatedCrop);
            dialog.setPrefillData(prefill, goalId);
        }
        dialog.setOnGoalSavedListener((goalData, editingGoalId) -> saveGoalToFirebase(goalData, editingGoalId));
        dialog.show(getParentFragmentManager(), "AddGoalDialogFragment");
    }

    private void saveGoalToFirebase(Bundle goalData, @Nullable String editingGoalId) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference goalsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(userId).child("goals");
        String goalId = editingGoalId != null ? editingGoalId : goalsRef.push().getKey();
        GoalAdapter.Goal goal = new GoalAdapter.Goal();
        goal.name = goalData.getString("name");
        goal.type = goalData.getString("type");
        goal.priority = goalData.getString("priority");
        goal.targetDate = goalData.getString("targetDate");
        goal.status = "In Progress";
        goal.progress = 0;
        goal.targetQuantity = goalData.getString("targetQuantity");
        goal.relatedCrop = goalData.getString("relatedCrop");
        goalsRef.child(goalId).setValue(goal);
        // Add reminder for this goal
        addReminderForGoal(goal);
        // Optionally show a toast
        new Handler().postDelayed(this::loadGoalsFromFirebase, 300);
        Toast.makeText(requireContext(), "Goal saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void addReminderForGoal(GoalAdapter.Goal goal) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference remindersRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(userId).child("reminders");
        // Parse dateMillis from goal.targetDate and goal.targetTime (format: MMM dd, yyyy and HH:mm)
        long dateMillis = 0L;
        try {
            String dateStr = goal.targetDate;
            String timeStr = goal.targetTime;
            java.text.SimpleDateFormat sdf;
            java.util.Date date;
            if (timeStr != null && !timeStr.isEmpty()) {
                sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
                date = sdf.parse(dateStr + " " + timeStr);
            } else {
                sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                date = sdf.parse(dateStr);
            }
            if (date != null) dateMillis = date.getTime();
        } catch (Exception e) {
            // fallback: use current time
            dateMillis = System.currentTimeMillis();
        }
        java.util.HashMap<String, Object> reminder = new java.util.HashMap<>();
        reminder.put("goalName", goal.name);
        reminder.put("activityType", goal.type);
        reminder.put("cropName", goal.relatedCrop);
        reminder.put("dateMillis", dateMillis);
        reminder.put("status", "pending");
        remindersRef.push().setValue(reminder);
    }

    private void loadGoalsFromFirebase() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference goalsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(userId).child("goals");
        goalsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                goalList.clear();
                goalIds.clear();
                for (com.google.firebase.database.DataSnapshot goalSnap : snapshot.getChildren()) {
                    GoalAdapter.Goal goal = goalSnap.getValue(GoalAdapter.Goal.class);
                    if (goal != null) {
                        goalList.add(goal);
                        goalIds.add(goalSnap.getKey());
                    }
                }
                goalAdapter.setGoals(goalList, goalIds);
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // Optionally show a toast
            }
        });
    }

    private void deleteGoalFromFirebase(String goalId) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference goalsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(userId).child("goals");
        goalsRef.child(goalId).removeValue();
    }
} 