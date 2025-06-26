package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.adapters.ReminderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class RemindersFragment extends Fragment implements ReminderAdapter.OnReminderListener {
    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private DatabaseReference remindersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders, container, false);
        recyclerView = view.findViewById(R.id.reminders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList, this);
        recyclerView.setAdapter(adapter);
        loadReminders();
        FloatingActionButton fabAddReminder = view.findViewById(R.id.fabAddReminder);
        fabAddReminder.setOnClickListener(v -> {
            AddReminderDialog dialog = new AddReminderDialog();
            dialog.setOnReminderAddedListener(this::loadReminders);
            dialog.show(getParentFragmentManager(), "AddReminderDialog");
        });
        return view;
    }

    private void loadReminders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        remindersRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("reminders");
        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reminderList.clear();
                for (DataSnapshot reminderSnap : snapshot.getChildren()) {
                    Reminder reminder = reminderSnap.getValue(Reminder.class);
                    if (reminder != null && "pending".equals(reminder.status)) {
                        reminder.id = reminderSnap.getKey(); // Store the Firebase key
                        reminderList.add(reminder);
                    }
                }
                adapter.setReminders(reminderList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load reminders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMarkAsDone(Reminder reminder) {
        if (reminder != null && reminder.id != null) {
            remindersRef.child(reminder.id).child("status").setValue("done")
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Reminder marked as done.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update reminder.", Toast.LENGTH_SHORT).show());
        }
    }

    // AddReminderDialog for manual reminders
    public static class AddReminderDialog extends androidx.fragment.app.DialogFragment {
        private com.google.android.material.textfield.TextInputEditText inputTitle, inputNotes;
        private com.google.android.material.button.MaterialButton btnSelectDate, btnSelectTime, btnSaveReminder;
        private com.google.android.material.textfield.MaterialAutoCompleteTextView inputActivityType;
        private com.google.android.material.textfield.MaterialAutoCompleteTextView inputCrop;
        private Calendar calendar = Calendar.getInstance();
        private OnReminderAddedListener listener;
        private long selectedDateMillis = -1;
        private long selectedTimeMillis = -1;
        private List<String> cropNames = new ArrayList<>();
        public interface OnReminderAddedListener { void onReminderAdded(); }
        public void setOnReminderAddedListener(OnReminderAddedListener l) { this.listener = l; }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_add_reminder, container, false);
            inputTitle = view.findViewById(R.id.inputReminderTitle);
            inputNotes = view.findViewById(R.id.inputReminderNotes);
            btnSelectDate = view.findViewById(R.id.btnSelectDate);
            btnSelectTime = view.findViewById(R.id.btnSelectTime);
            btnSaveReminder = view.findViewById(R.id.btnSaveReminder);
            inputActivityType = view.findViewById(R.id.inputActivityType);
            inputCrop = view.findViewById(R.id.inputCrop);
            String[] types = {"Planting", "Watering", "Fertilizing", "Pest Control", "Weeding", "Pruning", "Harvesting", "Other"};
            inputActivityType.setAdapter(new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, types));
            btnSelectDate.setOnClickListener(v -> showDatePicker());
            btnSelectTime.setOnClickListener(v -> showTimePicker());
            btnSaveReminder.setOnClickListener(v -> saveReminder());
            fetchCropNames();
            return view;
        }
        private void showDatePicker() {
            DatePickerDialog dlg = new DatePickerDialog(getContext(), (view, year, month, day) -> {
                calendar.set(year, month, day);
                selectedDateMillis = calendar.getTimeInMillis();
                btnSelectDate.setText(android.text.format.DateFormat.format("MMM dd, yyyy", calendar));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dlg.show();
        }
        private void showTimePicker() {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            android.app.TimePickerDialog dlg = new android.app.TimePickerDialog(getContext(), (view, h, m) -> {
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, m);
                selectedTimeMillis = calendar.getTimeInMillis();
                btnSelectTime.setText(String.format("%02d:%02d", h, m));
            }, hour, minute, true);
            dlg.show();
        }
        private void fetchCropNames() {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference cropsRef = FirebaseDatabase.getInstance().getReference("crops").child(userId);
            cropsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    cropNames.clear();
                    cropNames.add("General");
                    for (DataSnapshot cropSnap : snapshot.getChildren()) {
                        String name = cropSnap.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) cropNames.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cropNames);
                    inputCrop.setAdapter(adapter);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        private void saveReminder() {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("reminders");
            String key = ref.push().getKey();
            Reminder reminder = new Reminder();
            reminder.goalName = inputTitle.getText() != null ? inputTitle.getText().toString().trim() : "";
            reminder.activityType = inputActivityType.getText() != null ? inputActivityType.getText().toString() : "";
            // Combine date and time
            long dateTimeMillis = selectedDateMillis > 0 ? selectedDateMillis : calendar.getTimeInMillis();
            if (selectedTimeMillis > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dateTimeMillis);
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTimeInMillis(selectedTimeMillis);
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                dateTimeMillis = cal.getTimeInMillis();
            }
            reminder.dateMillis = dateTimeMillis;
            reminder.status = "pending";
            reminder.cropName = inputCrop.getText() != null ? inputCrop.getText().toString().trim() : "";
            reminder.notes = inputNotes.getText() != null ? inputNotes.getText().toString().trim() : "";
            ref.child(key).setValue(reminder)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Reminder added!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onReminderAdded();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add reminder", Toast.LENGTH_SHORT).show());
        }
        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                android.view.Window window = dialog.getWindow();
                android.graphics.Point size = new android.graphics.Point();
                window.getWindowManager().getDefaultDisplay().getSize(size);
                int width = (int) (size.x * 0.9);
                window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }

    public static class Reminder {
        public String id;
        public String goalName;
        public String activityType;
        public String cropName;
        public long dateMillis;
        public String status;
        public String notes;
        public Reminder() {}
    }
} 