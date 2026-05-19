package com.example.model;

public class Employee {
    private int id;
    private int positionId;
    private String employeeName;
    private String grade;          // position_name (JOIN)
    private String residentNumber;
    private String education;
    private String department;
    private String phoneNumber;
    private String email;
    private String hireDate;
    private int salary;            // position.salary (JOIN)

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
    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }
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
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }

    @Override
    public String toString() { return employeeName; }
}
