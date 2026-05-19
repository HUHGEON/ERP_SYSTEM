package com.example.model;

public class SeminarEvaluation {
    private int id;
    private int seminarId;
    private String seminarName;
    private int employeeId;
    private String employeeName;
    private double rating;
    private String comment;

    public SeminarEvaluation() {}

    public SeminarEvaluation(int id, int seminarId, String seminarName, int employeeId, String employeeName, double rating, String comment) {
        this.id = id;
        this.seminarId = seminarId;
        this.seminarName = seminarName;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.rating = rating;
        this.comment = comment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSeminarId() { return seminarId; }
    public void setSeminarId(int seminarId) { this.seminarId = seminarId; }
    public String getSeminarName() { return seminarName; }
    public void setSeminarName(String seminarName) { this.seminarName = seminarName; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
