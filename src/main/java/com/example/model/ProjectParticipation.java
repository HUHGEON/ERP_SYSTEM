package com.example.model;

public class ProjectParticipation {
    private int id;
    private int projectId;
    private String projectName;
    private int developerId;
    private String developerName;
    private String projectRole;
    private String startDate;
    private String endDate;

    public ProjectParticipation() {}

    public ProjectParticipation(int id, int projectId, String projectName, int developerId,
                                String developerName, String projectRole, String startDate, String endDate) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.developerId = developerId;
        this.developerName = developerName;
        this.projectRole = projectRole;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }
    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }
    public String getProjectRole() { return projectRole; }
    public void setProjectRole(String projectRole) { this.projectRole = projectRole; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public String toString() { return id + " - " + projectName + " (" + developerName + ")"; }
}
