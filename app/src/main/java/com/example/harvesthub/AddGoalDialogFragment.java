package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddGoalDialogFragment extends DialogFragment {
    private TextInputEditText goalNameEditText;
    private TextInputEditText targetQuantityEditText;
    private TextInputEditText goalDateEditText;
    private TextInputEditText goalTimeEditText;
    private TextInputEditText relatedCropEditText;
    private AutoCompleteTextView goalTypeSpinner;
    private AutoCompleteTextView prioritySpinner;
    private MaterialButton saveButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private OnGoalSavedListener listener;
    private Bundle prefillData;
    private String editingGoalId;
    private int selectedHour = -1;
    private int selectedMinute = -1;

    public interface OnGoalSavedListener {
        void onGoalSaved(Bundle goalData, @Nullable String editingGoalId);
    }

    public void setOnGoalSavedListener(OnGoalSavedListener listener) {
        this.listener = listener;
    }

    public void setPrefillData(Bundle data, @Nullable String goalId) {
        this.prefillData = data;
        this.editingGoalId = goalId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_goal, container, false);

        // Initialize views
        goalNameEditText = view.findViewById(R.id.edit_text_goal_name);
        targetQuantityEditText = view.findViewById(R.id.edit_text_target_quantity);
        goalDateEditText = view.findViewById(R.id.edit_text_goal_date);
        goalTimeEditText = view.findViewById(R.id.edit_text_goal_time);
        relatedCropEditText = view.findViewById(R.id.edit_text_related_crop);
        goalTypeSpinner = view.findViewById(R.id.spinner_goal_type);
        prioritySpinner = view.findViewById(R.id.spinner_priority);
        saveButton = view.findViewById(R.id.button_save_goal);

        // Initialize calendar and date formatter
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        // Setup spinners
        setupSpinners();

        // Setup date and time pickers
        setupDatePicker();
        setupTimePicker();

        // Setup save button
        setupSaveButton();

        if (prefillData != null) {
            goalNameEditText.setText(prefillData.getString("name", ""));
            targetQuantityEditText.setText(prefillData.getString("targetQuantity", ""));
            goalDateEditText.setText(prefillData.getString("targetDate", ""));
            goalTimeEditText.setText(prefillData.getString("targetTime", ""));
            relatedCropEditText.setText(prefillData.getString("relatedCrop", ""));
            setSpinnerSelection(goalTypeSpinner, prefillData.getString("type", ""));
            setSpinnerSelection(prioritySpinner, prefillData.getString("priority", ""));
        }

        return view;
    }

    private void setupSpinners() {
        // Goal Type Spinner
        String[] goalTypes = {"Harvest", "Planting", "Maintenance", "Sales", "Other"};
        ArrayAdapter<String> goalTypeAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            goalTypes
        );
        goalTypeSpinner.setAdapter(goalTypeAdapter);

        // Priority Spinner
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priorities
        );
        prioritySpinner.setAdapter(priorityAdapter);
    }

    private void setupDatePicker() {
        goalDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    goalDateEditText.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupTimePicker() {
        goalTimeEditText.setOnClickListener(v -> {
            int hour = selectedHour >= 0 ? selectedHour : calendar.get(Calendar.HOUR_OF_DAY);
            int minute = selectedMinute >= 0 ? selectedMinute : calendar.get(Calendar.MINUTE);
            new android.app.TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute1;
                // Format time as HH:mm (24-hour)
                String formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                goalTimeEditText.setText(formatted);
            }, hour, minute, true).show();
        });
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            // Validate inputs
            if (validateInputs()) {
                // Create goal object and save
                saveGoal();
                dismiss();
            }
        });
    }

    private boolean validateInputs() {
        if (goalNameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a goal name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (targetQuantityEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a target quantity", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (goalDateEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please select a deadline", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (goalTimeEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (goalTypeSpinner.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please select a goal type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (prioritySpinner.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please select a priority level", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveGoal() {
        String goalName = goalNameEditText.getText().toString().trim();
        String targetQuantity = targetQuantityEditText.getText().toString().trim();
        String goalDate = goalDateEditText.getText().toString().trim();
        String goalTime = goalTimeEditText.getText().toString().trim();
        String relatedCrop = relatedCropEditText.getText().toString().trim();
        String goalType = goalTypeSpinner.getText().toString().trim();
        String priority = prioritySpinner.getText().toString().trim();

        Bundle data = new Bundle();
        data.putString("name", goalName);
        data.putString("type", goalType);
        data.putString("priority", priority);
        data.putString("targetDate", goalDate);
        data.putString("targetTime", goalTime);
        data.putString("targetQuantity", targetQuantity);
        data.putString("relatedCrop", relatedCrop);
        if (listener != null) listener.onGoalSaved(data, editingGoalId);
    }

    private void setSpinnerSelection(AutoCompleteTextView spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }
} 