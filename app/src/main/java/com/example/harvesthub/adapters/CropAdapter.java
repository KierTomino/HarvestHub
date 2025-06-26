package com.example.harvesthub.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harvesthub.R;
import com.example.harvesthub.models.Crop;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private final List<Crop> crops;
    private final OnCropActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final boolean isQuickView;

    public interface OnCropActionListener {
        void onAddProgressPhoto(Crop crop);
        void onLogHarvest(Crop crop);
        void onViewDetails(Crop crop);
        void onGotoYieldTracker(Crop crop);
    }

    public CropAdapter(List<Crop> crops, OnCropActionListener listener, boolean isQuickView) {
        this.crops = crops;
        this.listener = listener;
        this.isQuickView = isQuickView;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isQuickView ? R.layout.item_crop_quick_view : R.layout.item_crop;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = crops.get(position);
        holder.bind(crop);
    }

    @Override
    public int getItemCount() {
        return crops.size();
    }

    class CropViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cropImageView;
        private final TextView cropNameText;
        private final View statusIndicator;
        private final TextView statusText;
        private final Button viewDetailsButton;

        CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropImageView = itemView.findViewById(R.id.cropImageView);
            cropNameText = itemView.findViewById(R.id.cropNameText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            statusText = itemView.findViewById(R.id.statusText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }

        void bind(Crop crop) {
            cropNameText.setText(crop.getName());
            long currentTime = System.currentTimeMillis();
            long expectedHarvestDate = crop.getExpectedHarvestDate();
            long plantedDate = crop.getDatePlanted();
            boolean isHarvested = crop.isHarvested();

            // Use Calendar to compare year, month, day for 'ready today'
            java.util.Calendar calNow = java.util.Calendar.getInstance();
            calNow.setTimeInMillis(currentTime);
            int nowYear = calNow.get(java.util.Calendar.YEAR);
            int nowMonth = calNow.get(java.util.Calendar.MONTH);
            int nowDay = calNow.get(java.util.Calendar.DAY_OF_MONTH);

            java.util.Calendar calHarvest = java.util.Calendar.getInstance();
            calHarvest.setTimeInMillis(expectedHarvestDate);
            int harvestYear = calHarvest.get(java.util.Calendar.YEAR);
            int harvestMonth = calHarvest.get(java.util.Calendar.MONTH);
            int harvestDay = calHarvest.get(java.util.Calendar.DAY_OF_MONTH);

            boolean isReadyToday = !isHarvested && nowYear == harvestYear && nowMonth == harvestMonth && nowDay == harvestDay;
            boolean isOverdue = !isHarvested && (calNow.after(calHarvest));
            boolean isReadyOrOverdue = isReadyToday || isOverdue;

            // Debug log for troubleshooting
            android.util.Log.d("CropAdapter", "Crop: " + crop.getName() +
                ", Planted: " + new java.util.Date(plantedDate) +
                ", Harvest: " + new java.util.Date(expectedHarvestDate) +
                ", isHarvested: " + isHarvested +
                ", isReadyToday: " + isReadyToday +
                ", isOverdue: " + isOverdue);

            // Set planted and harvest date
            TextView plantedDateText = itemView.findViewById(R.id.plantedDateText);
            TextView harvestDateText = itemView.findViewById(R.id.harvestDateText);
            if (plantedDateText != null) {
                plantedDateText.setText("Planted: " + dateFormat.format(new Date(plantedDate)));
            }
            if (harvestDateText != null) {
                harvestDateText.setText("Harvest: " + dateFormat.format(new Date(expectedHarvestDate)));
            }

            // Set progress bar
            ProgressBar progressBar = itemView.findViewById(R.id.progressBar);
            if (progressBar != null) {
                int progress = 0;
                if (expectedHarvestDate > plantedDate) {
                    long total = expectedHarvestDate - plantedDate;
                    long done = Math.min(currentTime, expectedHarvestDate) - plantedDate;
                    progress = (int) (100 * done / total);
                    progress = Math.max(0, Math.min(progress, 100));
                }
                progressBar.setProgress(progress);
            }

            // Show/hide indicator
            View statusLayout = itemView.findViewById(R.id.statusLayout);
            TextView statusTextView = itemView.findViewById(R.id.statusText);
            View statusIndicatorView = itemView.findViewById(R.id.statusIndicator);
            if (statusLayout != null && statusTextView != null && statusIndicatorView != null) {
                statusLayout.setVisibility(View.VISIBLE);
                if (isReadyOrOverdue) {
                    statusTextView.setText("Ready to Harvest!");
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.status_ready));
                    statusIndicatorView.setBackgroundResource(R.drawable.status_ready_indicator);
                } else {
                    statusTextView.setText("Not Ready");
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                    statusIndicatorView.setBackgroundResource(R.drawable.status_growing_indicator);
                }
            }

            // Show image if available
            if (crop.getProgressPhotos() != null && !crop.getProgressPhotos().isEmpty()) {
                try {
                    String base64Photo = crop.getProgressPhotos().get(0);
                    byte[] imageBytes = Base64.decode(base64Photo, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    cropImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    cropImageView.setImageResource(R.drawable.ic_camera);
                }
            } else {
                cropImageView.setImageResource(R.drawable.ic_camera);
            }

            // Set days left to harvest
            TextView daysLeftText = itemView.findViewById(R.id.daysLeftText);
            if (daysLeftText != null) {
                // Convert both dates to midnight
                java.util.Calendar calNowMidnight = java.util.Calendar.getInstance();
                calNowMidnight.setTimeInMillis(currentTime);
                calNowMidnight.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calNowMidnight.set(java.util.Calendar.MINUTE, 0);
                calNowMidnight.set(java.util.Calendar.SECOND, 0);
                calNowMidnight.set(java.util.Calendar.MILLISECOND, 0);

                java.util.Calendar calHarvestMidnight = java.util.Calendar.getInstance();
                calHarvestMidnight.setTimeInMillis(expectedHarvestDate);
                calHarvestMidnight.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calHarvestMidnight.set(java.util.Calendar.MINUTE, 0);
                calHarvestMidnight.set(java.util.Calendar.SECOND, 0);
                calHarvestMidnight.set(java.util.Calendar.MILLISECOND, 0);

                long daysLeft = (calHarvestMidnight.getTimeInMillis() - calNowMidnight.getTimeInMillis()) / (1000 * 60 * 60 * 24);

                if (isReadyToday) {
                    daysLeftText.setText("Ready to harvest today!");
                } else if (isOverdue) {
                    daysLeftText.setText("Overdue for harvest");
                } else {
                    daysLeftText.setText(daysLeft + " days left to harvest");
                }
            }

            // View Details button
            viewDetailsButton.setOnClickListener(v -> listener.onViewDetails(crop));
        }
    }
} 