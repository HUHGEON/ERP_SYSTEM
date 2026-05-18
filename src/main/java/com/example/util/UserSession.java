package com.example.util;

public class UserSession {
    private static final UserSession INSTANCE = new UserSession();

    private int employeeId;
    private String name;
    private String department;
    private boolean admin;

    private UserSession() {}

    public static UserSession getInstance() { return INSTANCE; }

    public void init(int employeeId, String name, String department) {
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.admin = "경영관리".equals(department);
    }

    public void clear() {
        employeeId = 0;
        name = null;
        department = null;
        admin = false;
    }

    public int getEmployeeId() { return employeeId; }
    public String getName()    { return name; }
    public String getDepartment() { return department; }
    public boolean isAdmin()   { return admin; }
}
