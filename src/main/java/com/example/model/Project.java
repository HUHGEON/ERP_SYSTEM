package com.example.model;

public class Project {
    private int id;
    private int customerId;
    private String customerName;
    private String projectName;
    private String startDate;
    private String endDate;

    public Project() {}

    public Project(int id, int customerId, String customerName, String projectName, String startDate, String endDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.projectName = projectName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() {
        return (endDate == null || endDate.isEmpty()) ? "진행중" : "완료";
    }

    @Override
    public String toString() { return projectName != null ? projectName : String.valueOf(id); }
}
