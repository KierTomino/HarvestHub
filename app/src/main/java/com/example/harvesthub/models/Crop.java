package com.example.harvesthub.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Crop implements Serializable {
    private String id;
    private String name;
    private long datePlanted;
    private long expectedHarvestDate;
    private long actualHarvestDate;
    private double harvestYield;
    private String yieldUnit; // kg, pieces, etc.
    private List<String> progressPhotos; // Base64 encoded images
    private String userId;
    private String status = "ACTIVE";

    public Crop() {
        this.progressPhotos = new ArrayList<>();
    }

    public Crop(String name, long datePlanted, long expectedHarvestDate, String userId) {
        this.name = name;
        this.datePlanted = datePlanted;
        this.expectedHarvestDate = expectedHarvestDate;
        this.userId = userId;
        this.progressPhotos = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDatePlanted() {
        return datePlanted;
    }

    public void setDatePlanted(long datePlanted) {
        this.datePlanted = datePlanted;
    }

    public long getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public void setExpectedHarvestDate(long expectedHarvestDate) {
        this.expectedHarvestDate = expectedHarvestDate;
    }

    public long getActualHarvestDate() {
        return actualHarvestDate;
    }

    public void setActualHarvestDate(long actualHarvestDate) {
        this.actualHarvestDate = actualHarvestDate;
    }

    public double getHarvestYield() {
        return harvestYield;
    }

    public void setHarvestYield(double harvestYield) {
        this.harvestYield = harvestYield;
    }

    public String getYieldUnit() {
        return yieldUnit;
    }

    public void setYieldUnit(String yieldUnit) {
        this.yieldUnit = yieldUnit;
    }

    public List<String> getProgressPhotos() {
        return progressPhotos;
    }

    public void setProgressPhotos(List<String> progressPhotos) {
        this.progressPhotos = progressPhotos;
    }

    public void addProgressPhoto(String base64Photo) {
        this.progressPhotos.add(base64Photo);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getPlantingToHarvestDuration() {
        if (actualHarvestDate > 0 && datePlanted > 0) {
            return (actualHarvestDate - datePlanted) / (1000 * 60 * 60 * 24); // Duration in days
        }
        return -1;
    }

    public boolean isHarvested() {
        return actualHarvestDate > 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 