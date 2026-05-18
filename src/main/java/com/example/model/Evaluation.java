package com.example.model;

public class Evaluation {
    private int id;
    private int participationId;
    private String participationCategory;
    private String employeeName;
    private String projectName;
    private String projectRole;

    public Evaluation() {}

    public Evaluation(int id, int participationId, String participationCategory) {
        this.id = id;
        this.participationId = participationId;
        this.participationCategory = participationCategory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getParticipationId() { return participationId; }
    public void setParticipationId(int participationId) { this.participationId = participationId; }
    public String getParticipationCategory() { return participationCategory; }
    public void setParticipationCategory(String participationCategory) { this.participationCategory = participationCategory; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectRole() { return projectRole; }
    public void setProjectRole(String projectRole) { this.projectRole = projectRole; }

    @Override
    public String toString() { return id + " (" + participationCategory + ")"; }
}
