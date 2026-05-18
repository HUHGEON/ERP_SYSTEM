package com.example.model;

public class Study {
    private int id;
    private String studyName;
    private String category;

    public Study() {}

    public Study(int id, String studyName, String category) {
        this.id = id;
        this.studyName = studyName;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String studyName) { this.studyName = studyName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() { return studyName != null ? studyName : String.valueOf(id); }
}
