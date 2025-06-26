package com.example.harvesthub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.harvesthub.R;
import com.example.harvesthub.models.Plant;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<Plant> plants;
    private OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public PlantAdapter(List<Plant> plants, OnPlantClickListener listener) {
        this.plants = plants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);
        holder.bind(plant);
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public void updatePlants(List<Plant> newPlants) {
        this.plants = newPlants;
        notifyDataSetChanged();
    }

    public void filterPlants(List<Plant> filteredPlants) {
        this.plants = filteredPlants;
        notifyDataSetChanged();
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {
        private TextView plantNameText;
        private TextView scientificNameText;
        private TextView descriptionText;
        private TextView growingSeasonText;
        private TextView careRequirementsText;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantNameText = itemView.findViewById(R.id.plantNameText);
            scientificNameText = itemView.findViewById(R.id.scientificNameText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            growingSeasonText = itemView.findViewById(R.id.growingSeasonText);
            careRequirementsText = itemView.findViewById(R.id.careRequirementsText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlantClick(plants.get(position));
                }
            });
        }

        public void bind(Plant plant) {
            plantNameText.setText(plant.getName());
            scientificNameText.setText(plant.getScientificName());
            descriptionText.setText(plant.getDescription());
            growingSeasonText.setText("Growing Season: " + plant.getGrowingSeason());
            careRequirementsText.setText("Care: " + plant.getCareRequirements());
        }
    }
} 