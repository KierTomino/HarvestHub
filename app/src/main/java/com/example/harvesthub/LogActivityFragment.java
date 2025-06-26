package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class LogActivityFragment extends Fragment {
    private RecyclerView activityHistoryRecyclerView;
    private FloatingActionButton fabAddActivity;
    private ActivityHistoryAdapter activityHistoryAdapter;
    private List<ActivityLog> activityLogs;

    public LogActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_activity, container, false);

        activityHistoryRecyclerView = view.findViewById(R.id.activity_history_recycler_view);
        fabAddActivity = view.findViewById(R.id.fab_add_activity);

        // Initialize RecyclerView
        activityLogs = new ArrayList<>();
        activityHistoryAdapter = new ActivityHistoryAdapter(activityLogs, this::showActivityDetails);
        activityHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityHistoryRecyclerView.setAdapter(activityHistoryAdapter);

        // Load activity history from Firebase
        loadActivityHistoryFromFirebase();

        // Set up FAB click listener
        fabAddActivity.setOnClickListener(v -> showAddActivityDialog());

        return view;
    }

    private void loadActivityHistoryFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("activity_logs").child(userId);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                activityLogs.clear();
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    ActivityLogFirebase log = logSnap.getValue(ActivityLogFirebase.class);
                    if (log != null) {
                        activityLogs.add(new ActivityLog(
                            log.activityType,
                            log.description,
                            log.date,
                            log.notes,
                            log.photoBase64
                        ));
                    }
                }
                activityHistoryAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Error loading activities", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddActivityDialog() {
        AddActivityDialog dialog = new AddActivityDialog();
        dialog.setOnActivityLoggedListener(this::loadActivityHistoryFromFirebase);
        dialog.show(getChildFragmentManager(), "AddActivityDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityHistoryFromFirebase();
    }

    private void showActivityDetails(ActivityLog log) {
        ActivityLogDetailsDialogFragment dialog = ActivityLogDetailsDialogFragment.newInstance(log);
        dialog.show(getChildFragmentManager(), "ActivityLogDetailsDialog");
    }
}

class ActivityLog {
    private String type;
    private String description;
    private String date;
    private String notes;
    private String photoBase64;

    public ActivityLog(String type, String description, String date, String notes, String photoBase64) {
        this.type = type;
        this.description = description;
        this.date = date;
        this.notes = notes;
        this.photoBase64 = photoBase64;
    }

    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getNotes() { return notes; }
    public String getPhotoBase64() { return photoBase64; }
}

// Firebase model class
class ActivityLogFirebase {
    public String activityType, description, date, notes, photoBase64;
    public ActivityLogFirebase() {}
}

class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {
    private List<ActivityLog> activityLogs;
    private final OnLogClickListener logClickListener;

    public interface OnLogClickListener {
        void onLogClick(ActivityLog log);
    }

    public ActivityHistoryAdapter(List<ActivityLog> activityLogs, OnLogClickListener logClickListener) {
        this.activityLogs = activityLogs;
        this.logClickListener = logClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ActivityLog activityLog = activityLogs.get(position);
        holder.activityType.setText(activityLog.getType());
        holder.activityDescription.setText(activityLog.getDescription());
        holder.activityDate.setText(activityLog.getDate());
        if (activityLog.getNotes() != null && !activityLog.getNotes().isEmpty()) {
            holder.activityNotes.setVisibility(View.VISIBLE);
            holder.activityNotes.setText(activityLog.getNotes());
        } else {
            holder.activityNotes.setVisibility(View.GONE);
        }
        int iconResource = R.drawable.ic_activity; // Default icon
        String type = activityLog.getType().toLowerCase();
        switch (type) {
            case "planting": iconResource = R.drawable.ic_planting; break;
            case "watering": iconResource = R.drawable.ic_watering; break;
            case "fertilizing": iconResource = R.drawable.ic_fertilizing; break;
            case "harvesting": iconResource = R.drawable.ic_harvesting; break;
            case "pest control": iconResource = R.drawable.ic_pest_control; break;
            case "pruning": iconResource = R.drawable.ic_pruning; break;
        }
        holder.activityTypeIcon.setImageResource(iconResource);
        // Remove all image logic
        holder.itemView.setOnClickListener(v -> logClickListener.onLogClick(activityLog));
    }

    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView activityTypeIcon;
        TextView activityType;
        TextView activityDescription;
        TextView activityDate;
        TextView activityNotes;
        ViewHolder(View view) {
            super(view);
            activityTypeIcon = view.findViewById(R.id.activity_type_icon);
            activityType = view.findViewById(R.id.activity_type);
            activityDescription = view.findViewById(R.id.activity_description);
            activityDate = view.findViewById(R.id.activity_date);
            activityNotes = view.findViewById(R.id.activity_notes);
        }
    }
} 