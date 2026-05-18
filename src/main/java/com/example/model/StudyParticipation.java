package com.example.model;

public class StudyParticipation {
    private int id;
    private int studyId;
    private String studyName;
    private int employeeId;
    private String employeeName;

    public StudyParticipation() {}

    public StudyParticipation(int id, int studyId, String studyName, int employeeId, String employeeName) {
        this.id = id;
        this.studyId = studyId;
        this.studyName = studyName;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int studyId) { this.studyId = studyId; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String studyName) { this.studyName = studyName; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
