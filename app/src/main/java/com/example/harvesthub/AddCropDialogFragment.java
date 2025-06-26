package com.example.harvesthub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.harvesthub.models.Crop;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddCropDialogFragment extends DialogFragment {
    private ImageView cropImagePreview;
    private TextInputEditText inputCropName, inputDatePlanted, inputExpectedHarvestDate, inputExpectedYield;
    private AutoCompleteTextView inputYieldUnit;
    private Button btnSaveCrop;
    private String base64Image = null;
    private Date selectedDate = null;
    private Date expectedHarvestDate = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private AddCropListener listener;

    public interface AddCropListener {
        void onCropAdded(String cropName, Date datePlanted, String base64Image, Date expectedHarvestDate, String expectedYield, String yieldUnit);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof AddCropListener) {
            listener = (AddCropListener) getParentFragment();
        } else if (context instanceof AddCropListener) {
            listener = (AddCropListener) context;
        } else {
            throw new RuntimeException("Parent must implement AddCropListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_crop, container, false);
        cropImagePreview = view.findViewById(R.id.cropImagePreview);
        inputCropName = view.findViewById(R.id.inputCropName);
        inputDatePlanted = view.findViewById(R.id.inputDatePlanted);
        inputExpectedHarvestDate = view.findViewById(R.id.inputExpectedHarvestDate);
        inputExpectedYield = view.findViewById(R.id.inputExpectedYield);
        inputYieldUnit = view.findViewById(R.id.inputYieldUnit);
        btnSaveCrop = view.findViewById(R.id.btnSaveCrop);

        // Set up unit dropdown
        String[] units = {"kg", "pieces", "liters"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, units);
        inputYieldUnit.setAdapter(unitAdapter);

        cropImagePreview.setOnClickListener(v -> openImagePicker());
        inputDatePlanted.setOnClickListener(v -> showDatePicker(false));
        inputExpectedHarvestDate.setOnClickListener(v -> showDatePicker(true));
        btnSaveCrop.setOnClickListener(v -> saveCrop());

        return view;
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                        cropImagePreview.setImageBitmap(bitmap);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] imageBytes = baos.toByteArray();
                        base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker(boolean isExpectedHarvest) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isExpectedHarvest) {
                expectedHarvestDate = calendar.getTime();
                inputExpectedHarvestDate.setText(dateFormat.format(expectedHarvestDate));
            } else {
                selectedDate = calendar.getTime();
                inputDatePlanted.setText(dateFormat.format(selectedDate));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveCrop() {
        String cropName = inputCropName.getText().toString().trim();
        String expectedYieldStr = inputExpectedYield.getText().toString().trim();
        String yieldUnit = inputYieldUnit.getText().toString().trim();
        if (TextUtils.isEmpty(cropName)) {
            inputCropName.setError("Crop name required");
            return;
        }
        if (selectedDate == null) {
            inputDatePlanted.setError("Date required");
            return;
        }
        if (expectedHarvestDate == null) {
            inputExpectedHarvestDate.setError("Expected harvest date required");
            return;
        }
        // base64Image can be null (use placeholder in adapter if needed)
        if (listener != null) {
            listener.onCropAdded(cropName, selectedDate, base64Image, expectedHarvestDate, expectedYieldStr, yieldUnit);
        }
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }
} 