package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.widget.TextView;

public class AddActivityDialog extends DialogFragment {
    private EditText editTextTaskName, editTextDate, editTextNotes;
    private Spinner spinnerActivityType;
    private EditText editTextYieldAmount;
    private TextView labelYieldAmount;
    private Button buttonLogActivity, btnAddPhoto;
    private Calendar calendar;
    private OnActivityLoggedListener listener;
    private ImageView activityPhotoPreview;
    private String base64Photo = null;
    private Uri photoUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 2001;
    private String cropId;

    public interface OnActivityLoggedListener {
        void onActivityLogged();
    }

    public void setOnActivityLoggedListener(OnActivityLoggedListener listener) {
        this.listener = listener;
    }

    public void setCropId(String cropId) { this.cropId = cropId; }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_activity, container, false);

        editTextTaskName = view.findViewById(R.id.edit_text_task_name);
        editTextDate = view.findViewById(R.id.edit_text_date);
        editTextNotes = view.findViewById(R.id.edit_text_notes);
        spinnerActivityType = view.findViewById(R.id.spinner_activity_type);
        editTextYieldAmount = view.findViewById(R.id.edit_text_yield_amount);
        labelYieldAmount = view.findViewById(R.id.label_yield_amount);
        buttonLogActivity = view.findViewById(R.id.button_log_activity);
        activityPhotoPreview = view.findViewById(R.id.activityPhotoPreview);
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);

        calendar = Calendar.getInstance();

        // Set up Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.activity_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityType.setAdapter(adapter);

        // Set up DatePickerDialog
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Set up Log Activity Button
        buttonLogActivity.setOnClickListener(v -> logActivity());

        btnAddPhoto.setOnClickListener(v -> showPhotoOptions());
        activityPhotoPreview.setOnClickListener(v -> showPhotoOptions());

        return view;
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateEditText();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateEditText() {
        String format = "MMM dd, yyyy"; // e.g., Jan 01, 2023
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        editTextDate.setText(sdf.format(calendar.getTime()));
    }

    private void showPhotoOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take Photo
                        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        } else {
                            launchCamera();
                        }
                    } else {
                        // Choose from Gallery
                        pickImageFromGallery();
                    }
                })
                .show();
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile("activity_photo", ".jpg", requireActivity().getCacheDir());
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FragmentActivity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
                        setPhoto(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Failed to load photo", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                    setPhoto(bitmap);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to load photo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setPhoto(Bitmap bitmap) {
        activityPhotoPreview.setImageBitmap(bitmap);
        // Convert to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        base64Photo = Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void logActivity() {
        String taskName = editTextTaskName.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String activityType = spinnerActivityType.getSelectedItem().toString();
        String notes = editTextNotes.getText().toString().trim();
        String yieldAmount = editTextYieldAmount.getText().toString().trim();

        if (taskName.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Task Name and Date are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("activity_logs").child(userId);
        String key = dbRef.push().getKey();
        ActivityLogFirebase log = new ActivityLogFirebase(
            activityType,
            taskName,
            date,
            notes,
            base64Photo,
            yieldAmount,
            cropId
        );
        dbRef.child(key).setValue(log)
            .addOnSuccessListener(aVoid -> {
                if (cropId != null && activityType.toLowerCase().contains("harvest")) {
                    DatabaseReference cropRef = FirebaseDatabase.getInstance().getReference("crops").child(userId).child(cropId);
                    cropRef.child("status").setValue("HARVESTED");
                    if (!yieldAmount.isEmpty()) {
                        try {
                            double yield = Double.parseDouble(yieldAmount);
                            cropRef.child("harvestYield").setValue(yield);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                Toast.makeText(getContext(), "Activity logged to Firebase!", Toast.LENGTH_SHORT).show();
                updateRelatedReminders(activityType, calendar);
                if (listener != null) listener.onActivityLogged();
                dismiss();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to log activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void updateRelatedReminders(String activityType, Calendar loggedCalendar) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("reminders");

        // Calculate start and end of the day for the query
        Calendar startOfDay = (Calendar) loggedCalendar.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        long startMillis = startOfDay.getTimeInMillis();

        Calendar endOfDay = (Calendar) loggedCalendar.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);
        long endMillis = endOfDay.getTimeInMillis();

        remindersRef.orderByChild("dateMillis").startAt(startMillis).endAt(endMillis)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int updatedCount = 0;
                    for (DataSnapshot reminderSnap : snapshot.getChildren()) {
                        RemindersFragment.Reminder reminder = reminderSnap.getValue(RemindersFragment.Reminder.class);
                        if (reminder != null &&
                            "pending".equals(reminder.status) &&
                            activityType.equalsIgnoreCase(reminder.activityType)) {
                            
                            reminderSnap.getRef().child("status").setValue("done");
                            updatedCount++;
                        }
                    }
                    if (updatedCount > 0) {
                        Toast.makeText(getContext(), updatedCount + " related reminder(s) marked as done.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Could add a toast here for failure
                }
            });
    }

    // Firebase model class
    public static class ActivityLogFirebase {
        public String activityType, description, date, notes, photoBase64, yieldAmount, cropId;
        public ActivityLogFirebase() {}
        public ActivityLogFirebase(String activityType, String description, String date, String notes, String photoBase64, String yieldAmount, String cropId) {
            this.activityType = activityType;
            this.description = description;
            this.date = date;
            this.notes = notes;
            this.photoBase64 = photoBase64;
            this.yieldAmount = yieldAmount;
            this.cropId = cropId;
        }
    }
} 