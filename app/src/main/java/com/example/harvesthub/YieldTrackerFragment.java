package com.example.harvesthub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harvesthub.adapters.CropAdapter;
import com.example.harvesthub.models.Crop;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YieldTrackerFragment extends Fragment implements AddCropDialogFragment.AddCropListener {
    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<Crop> allCrops;
    private List<Crop> filteredCrops;
    private DatabaseReference cropsRef;
    private String currentUserId;
    private TabLayout tabLayout;
    private TextView emptyMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cropsRef = FirebaseDatabase.getInstance().getReference("crops").child(currentUserId);
        allCrops = new ArrayList<>();
        filteredCrops = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yield_tracker, container, false);

        cropsRecyclerView = view.findViewById(R.id.cropsRecyclerView);
        FloatingActionButton fabAddCrop = view.findViewById(R.id.fabAddCrop);
        tabLayout = view.findViewById(R.id.tabLayout);
        emptyMessage = view.findViewById(R.id.emptyMessage);

        cropAdapter = new CropAdapter(filteredCrops, new CropAdapter.OnCropActionListener() {
            @Override
            public void onAddProgressPhoto(Crop crop) {}
            @Override
            public void onLogHarvest(Crop crop) {
                AddActivityDialog dialog = new AddActivityDialog();
                dialog.setCropId(crop.getId());
                dialog.setOnActivityLoggedListener(() -> loadCrops());
                dialog.show(getChildFragmentManager(), "AddActivityDialog");
            }
            @Override
            public void onViewDetails(Crop crop) {
                CropDetailsDialogFragment dialog = CropDetailsDialogFragment.newInstance(crop);
                dialog.show(getChildFragmentManager(), "CropDetailsDialogFragment");
            }
            @Override
            public void onGotoYieldTracker(Crop crop) {
                // No action needed in YieldTrackerFragment
            }
        }, false);
        cropsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        cropsRecyclerView.setAdapter(cropAdapter);

        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText("Active Crops"));
        tabLayout.addTab(tabLayout.newTab().setText("Harvest History"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterAndShowCrops();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCrops();

        fabAddCrop.setOnClickListener(v -> {
            AddCropDialogFragment dialog = new AddCropDialogFragment();
            dialog.show(getChildFragmentManager(), "AddCropDialogFragment");
        });

        return view;
    }

    private void loadCrops() {
        cropsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCrops.clear();
                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    Crop crop = cropSnapshot.getValue(Crop.class);
                    if (crop != null) {
                        crop.setId(cropSnapshot.getKey());
                        allCrops.add(crop);
                    }
                }
                filterAndShowCrops();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading crops", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndShowCrops() {
        filteredCrops.clear();
        int selectedTab = tabLayout.getSelectedTabPosition();
        if (selectedTab == 0) { // Active Crops
            for (Crop crop : allCrops) {
                String status = crop.getStatus();
                if (status == null || status.equals("ACTIVE")) {
                    filteredCrops.add(crop);
                }
            }
        } else { // Harvest History
            for (Crop crop : allCrops) {
                String status = crop.getStatus();
                if ("HARVESTED".equals(status)) {
                    filteredCrops.add(crop);
                }
            }
        }
        cropAdapter.notifyDataSetChanged();
        if (emptyMessage != null) {
            emptyMessage.setVisibility(filteredCrops.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCropAdded(String cropName, Date datePlanted, String base64Image, Date expectedHarvestDate, String expectedYield, String yieldUnit) {
        Toast.makeText(getContext(), "onCropAdded called!", Toast.LENGTH_SHORT).show();
        String cropId = cropsRef.push().getKey();
        Crop crop = new Crop(cropName, datePlanted.getTime(), expectedHarvestDate.getTime(), currentUserId);
        crop.setId(cropId);
        if (base64Image != null) {
            crop.getProgressPhotos().add(base64Image);
        }
        if (expectedYield != null && !expectedYield.isEmpty()) {
            try {
                crop.setHarvestYield(Double.parseDouble(expectedYield));
            } catch (NumberFormatException e) {
                crop.setHarvestYield(0);
            }
        }
        crop.setYieldUnit(yieldUnit);
        cropsRef.child(cropId).setValue(crop)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Crop saved to Firebase!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save crop: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
} 