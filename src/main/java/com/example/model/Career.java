package com.example.model;

public class Career {
    private int id;
    private int employeeId;
    private String employeeName;
    private String companyName;
    private String startTime;
    private String endTime;

    public Career() {}

    public Career(int id, int employeeId, String employeeName, String companyName, String startTime, String endTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.companyName = companyName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
