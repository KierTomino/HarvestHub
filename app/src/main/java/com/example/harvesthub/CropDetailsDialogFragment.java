package com.example.harvesthub;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;

import com.example.harvesthub.models.Crop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

public class CropDetailsDialogFragment extends DialogFragment {
    private static final String ARG_CROP = "arg_crop";
    private Crop crop;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private DatabaseReference cropsRef;
    private String currentUserId;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int REQUEST_READ_IMAGE_PERMISSION = 1001;
    private LinearLayout activityLogContainer;

    public static CropDetailsDialogFragment newInstance(Crop crop) {
        CropDetailsDialogFragment fragment = new CropDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CROP, crop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            crop = (Crop) getArguments().getSerializable(ARG_CROP);
        }
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cropsRef = FirebaseDatabase.getInstance().getReference("crops").child(currentUserId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_crop_details, container, false);
        ImageView cropImage = view.findViewById(R.id.detailsCropImage);
        TextView cropName = view.findViewById(R.id.detailsCropName);
        TextView plantedDate = view.findViewById(R.id.detailsPlantedDate);
        TextView harvestInfo = view.findViewById(R.id.detailsHarvestInfo);
        TextView yield = view.findViewById(R.id.detailsYield);
        Button btnEdit = view.findViewById(R.id.btnEditCrop);
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        Button btnDelete = view.findViewById(R.id.btnDeleteCrop);
        Button btnClose = view.findViewById(R.id.btnCloseDetails);
        Button btnLogHarvest = view.findViewById(R.id.btnLogHarvest);
        activityLogContainer = view.findViewById(R.id.activityLogContainer);

        // Set crop info
        cropName.setText(crop.getName());
        plantedDate.setText("Planted: " + dateFormat.format(new Date(crop.getDatePlanted())));
        harvestInfo.setText("Expected Harvest: " + dateFormat.format(new Date(crop.getExpectedHarvestDate())));
        yield.setText("Expected Yield: " + crop.getHarvestYield() + " " + crop.getYieldUnit());

        LinearLayout statusLayout = view.findViewById(R.id.detailsStatusLayout);
        if (statusLayout != null) {
            long currentTime = System.currentTimeMillis();
            boolean isReady = !crop.isHarvested() && crop.getExpectedHarvestDate() <= currentTime;
            statusLayout.setVisibility(isReady ? View.VISIBLE : View.GONE);
        }

        if (crop.getProgressPhotos() != null && !crop.getProgressPhotos().isEmpty()) {
            try {
                String base64Photo = crop.getProgressPhotos().get(0);
                byte[] imageBytes = Base64.decode(base64Photo, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                cropImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                cropImage.setImageResource(R.drawable.ic_camera);
            }
        } else {
            cropImage.setImageResource(R.drawable.ic_camera);
        }

        updateTimeline(view);
        loadActivityLogs();

        btnEdit.setOnClickListener(v -> {
            if (crop != null && crop.getId() != null) {
                crop.setActualHarvestDate(System.currentTimeMillis());
                crop.setStatus("HARVESTED");
                cropsRef.child(crop.getId()).setValue(crop)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Crop marked as collected!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to mark as collected", Toast.LENGTH_SHORT).show());
            }
        });
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        if (crop == null || crop.getId() == null) {
                            Toast.makeText(getContext(), "Crop or Crop ID is missing!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), result.getData().getData());
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        String base64Photo = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                        crop.getProgressPhotos().add(base64Photo);
                        cropsRef.child(crop.getId()).setValue(crop)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Photo added!", Toast.LENGTH_SHORT).show();
                                updateTimeline(getView());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add photo", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        );
        btnAddPhoto.setOnClickListener(v -> {
            openImagePicker();
        });
        btnDelete.setOnClickListener(v -> {
            cropsRef.child(crop.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Crop deleted", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete crop", Toast.LENGTH_SHORT).show());
        });
        btnClose.setOnClickListener(v -> dismiss());

        if (btnLogHarvest != null) {
            boolean isHarvested = crop.isHarvested();
            btnLogHarvest.setVisibility(isHarvested ? View.GONE : View.VISIBLE);
            btnLogHarvest.setOnClickListener(v -> {
                AddActivityDialog dialog = new AddActivityDialog();
                dialog.setCropId(crop.getId());
                dialog.setOnActivityLoggedListener(() -> {
                    loadActivityLogs();
                    // Optionally refresh parent list if needed
                    if (getParentFragment() instanceof YieldTrackerFragment) {
                        ((YieldTrackerFragment) getParentFragment()).onResume();
                    }
                    dismiss();
                });
                dialog.show(getParentFragmentManager(), "AddActivityDialog");
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateTimeline(View rootView) {
        LinearLayout timelineContainer = rootView.findViewById(R.id.timelineContainer);
        timelineContainer.removeAllViews();
        for (String base64Photo : crop.getProgressPhotos()) {
            View timelineItem = LayoutInflater.from(getContext()).inflate(R.layout.item_timeline_photo, timelineContainer, false);
            ImageView photoView = timelineItem.findViewById(R.id.timelinePhoto);
            TextView dateView = timelineItem.findViewById(R.id.timelineDate);
            // Set photo
            byte[] imageBytes = Base64.decode(base64Photo, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            photoView.setImageBitmap(bitmap);
            // Set date (for now, just use crop.getDatePlanted())
            dateView.setText(dateFormat.format(new Date(crop.getDatePlanted())));
            timelineContainer.addView(timelineItem);
        }
    }

    private void loadActivityLogs() {
        if (crop == null || crop.getId() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("activity_logs").child(userId);
        logsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                activityLogContainer.removeAllViews();
                for (com.google.firebase.database.DataSnapshot logSnap : snapshot.getChildren()) {
                    AddActivityDialog.ActivityLogFirebase log = logSnap.getValue(AddActivityDialog.ActivityLogFirebase.class);
                    if (log != null && crop.getId().equals(log.cropId)) {
                        View logView = LayoutInflater.from(getContext()).inflate(R.layout.item_activity_history, activityLogContainer, false);
                        // Set icon based on activity type
                        ImageView icon = logView.findViewById(R.id.activity_type_icon);
                        int iconRes = R.drawable.ic_activity;
                        String type = log.activityType != null ? log.activityType.toLowerCase() : "";
                        if (type.contains("plant")) iconRes = R.drawable.ic_planting;
                        else if (type.contains("water")) iconRes = R.drawable.ic_watering;
                        else if (type.contains("fertiliz")) iconRes = R.drawable.ic_fertilizing;
                        else if (type.contains("harvest")) iconRes = R.drawable.ic_harvesting;
                        else if (type.contains("pest")) iconRes = R.drawable.ic_pest_control;
                        else if (type.contains("prun")) iconRes = R.drawable.ic_pruning;
                        icon.setImageResource(iconRes);
                        // Set text fields
                        ((TextView) logView.findViewById(R.id.activity_type)).setText(log.activityType);
                        ((TextView) logView.findViewById(R.id.activity_date)).setText(log.date);
                        ((TextView) logView.findViewById(R.id.activity_description)).setText(log.description);
                        // Show photo if available
                        ImageView photo = logView.findViewById(R.id.activity_photo);
                        if (log.photoBase64 != null && !log.photoBase64.isEmpty()) {
                            try {
                                byte[] imageBytes = android.util.Base64.decode(log.photoBase64, android.util.Base64.DEFAULT);
                                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                photo.setImageBitmap(bitmap);
                                photo.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                photo.setVisibility(View.GONE);
                            }
                        } else {
                            photo.setVisibility(View.GONE);
                        }
                        // Show notes if not empty
                        TextView notes = logView.findViewById(R.id.activity_notes);
                        if (log.notes != null && !log.notes.trim().isEmpty()) {
                            notes.setText(log.notes);
                            notes.setVisibility(View.VISIBLE);
                        } else {
                            notes.setVisibility(View.GONE);
                        }
                        activityLogContainer.addView(logView);
                    }
                }
                if (activityLogContainer.getChildCount() == 0) {
                    TextView empty = new TextView(getContext());
                    empty.setText("No activities logged for this crop.");
                    activityLogContainer.addView(empty);
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
} 