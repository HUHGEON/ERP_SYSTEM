package com.example.model;

public class Management {
    private int id;
    private String employeeName;
    private String permissionLevel;

    public Management() {}

    public Management(int id, String employeeName, String permissionLevel) {
        this.id = id;
        this.employeeName = employeeName;
        this.permissionLevel = permissionLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }

    @Override
    public String toString() { return employeeName; }
}
