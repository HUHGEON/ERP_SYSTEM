package com.example.model;

public class Output {
    private int id;
    private int projectId;
    private String projectName;
    private String outputType;
    private String outputName;

    public Output() {}

    public Output(int id, int projectId, String projectName, String outputType, String outputName) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.outputType = outputType;
        this.outputName = outputName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getOutputType() { return outputType; }
    public void setOutputType(String outputType) { this.outputType = outputType; }
    public String getOutputName() { return outputName; }
    public void setOutputName(String outputName) { this.outputName = outputName; }
}
