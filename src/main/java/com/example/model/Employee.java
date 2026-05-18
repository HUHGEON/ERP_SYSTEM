package com.example.model;

public class Employee {
    private int id;
    private String employeeName;
    private String grade;
    private String residentNumber;
    private String education;
    private String department;

    public Employee() {}

    public Employee(int id, String employeeName, String grade, String residentNumber, String education, String department) {
        this.id = id;
        this.employeeName = employeeName;
        this.grade = grade;
        this.residentNumber = residentNumber;
        this.education = education;
        this.department = department;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getResidentNumber() { return residentNumber; }
    public void setResidentNumber(String residentNumber) { this.residentNumber = residentNumber; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() { return employeeName; }
}
