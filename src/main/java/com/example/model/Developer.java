package com.example.model;

public class Developer {
    private int id;
    private String employeeName;
    private String tech;

    public Developer() {}

    public Developer(int id, String employeeName, String tech) {
        this.id = id;
        this.employeeName = employeeName;
        this.tech = tech;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getTech() { return tech; }
    public void setTech(String tech) { this.tech = tech; }

    @Override
    public String toString() { return employeeName; }
}
