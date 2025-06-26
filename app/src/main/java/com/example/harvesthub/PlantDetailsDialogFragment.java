package com.example.harvesthub;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.harvesthub.models.Plant;

public class PlantDetailsDialogFragment extends DialogFragment {

    private Plant plant;

    public static PlantDetailsDialogFragment newInstance(Plant plant) {
        PlantDetailsDialogFragment fragment = new PlantDetailsDialogFragment();
        fragment.plant = plant;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_plant_details, container, false);

        // Initialize views
        TextView plantNameText = view.findViewById(R.id.plantNameText);
        TextView scientificNameText = view.findViewById(R.id.scientificNameText);
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView growingSeasonText = view.findViewById(R.id.growingSeasonText);
        TextView careRequirementsText = view.findViewById(R.id.careRequirementsText);
        ImageButton closeButton = view.findViewById(R.id.closeButton);
        Button closeDialogButton = view.findViewById(R.id.closeDialogButton);

        // Set plant information
        if (plant != null) {
            plantNameText.setText(plant.getName());
            scientificNameText.setText(plant.getScientificName());
            descriptionText.setText(plant.getDescription());
            growingSeasonText.setText(plant.getGrowingSeason());
            careRequirementsText.setText(plant.getCareRequirements());
        }

        // Set click listeners
        closeButton.setOnClickListener(v -> dismiss());
        closeDialogButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set dialog width to 90% of screen width
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                window.setLayout(width, height);
            }
        }
    }
} 