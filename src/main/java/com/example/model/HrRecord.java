package com.example.model;

public class HrRecord {
    private int id;
    private int employeeId;
    private String employeeName;
    private String employmentData;
    private String promotionDate;

    public HrRecord() {}

    public HrRecord(int id, int employeeId, String employeeName, String employmentData, String promotionDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employmentData = employmentData;
        this.promotionDate = promotionDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmploymentData() { return employmentData; }
    public void setEmploymentData(String employmentData) { this.employmentData = employmentData; }
    public String getPromotionDate() { return promotionDate; }
    public void setPromotionDate(String promotionDate) { this.promotionDate = promotionDate; }
}
