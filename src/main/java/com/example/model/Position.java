package com.example.model;

public class Position {
    private int id;
    private String positionName;
    private int salary;
    private int annualLeaveDays;

    public Position() {}

    public Position(int id, String positionName, int salary, int annualLeaveDays) {
        this.id = id;
        this.positionName = positionName;
        this.salary = salary;
        this.annualLeaveDays = annualLeaveDays;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
    public int getAnnualLeaveDays() { return annualLeaveDays; }
    public void setAnnualLeaveDays(int annualLeaveDays) { this.annualLeaveDays = annualLeaveDays; }

    @Override
    public String toString() { return positionName; }
}
