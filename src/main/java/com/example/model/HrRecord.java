package com.example.model;

public class HrRecord {
    private int id;
    private int employeeId;
    private String employeeName;
    private int positionId;
    private String positionName;   // position_name (JOIN)
    private String promotionDate;

    public HrRecord() {}

    public HrRecord(int id, int employeeId, String employeeName, int positionId, String promotionDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.positionId = positionId;
        this.promotionDate = promotionDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getPromotionDate() { return promotionDate; }
    public void setPromotionDate(String promotionDate) { this.promotionDate = promotionDate; }
}
