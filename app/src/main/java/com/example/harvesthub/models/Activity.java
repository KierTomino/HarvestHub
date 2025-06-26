package com.example.harvesthub.models;

import java.util.Date;

public class Activity {
    private String type;
    private String description;
    private String notes;
    private Date date;

    public Activity(String type, String description, String notes, Date date) {
        this.type = type;
        this.description = description;
        this.notes = notes;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
} 