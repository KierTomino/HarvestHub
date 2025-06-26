package com.example.harvesthub.models;

public class Plant {
    private String id;
    private String name;
    private String scientificName;
    private String type;
    private String category;
    private String description;
    private String growingSeason;
    private String careRequirements;
    private String imageUrl;

    public Plant() {
        // Required empty constructor for Firebase
    }

    public Plant(String name, String scientificName, String type, String category, 
                 String description, String growingSeason, String careRequirements) {
        this.name = name;
        this.scientificName = scientificName;
        this.type = type;
        this.category = category;
        this.description = description;
        this.growingSeason = growingSeason;
        this.careRequirements = careRequirements;
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

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrowingSeason() {
        return growingSeason;
    }

    public void setGrowingSeason(String growingSeason) {
        this.growingSeason = growingSeason;
    }

    public String getCareRequirements() {
        return careRequirements;
    }

    public void setCareRequirements(String careRequirements) {
        this.careRequirements = careRequirements;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 