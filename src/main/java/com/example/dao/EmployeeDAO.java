package com.example.dao;

import com.example.model.Employee;
import com.example.util.MaskingUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> search(String name, String grade, String department) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM employee WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND employee_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (grade != null && !grade.isEmpty()) {
            sql.append(" AND grade = ?");
            params.add(grade);
        }
        if (department != null && !department.isEmpty()) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("employee_name"),
                        rs.getString("grade"),
                        MaskingUtil.maskResidentNumber(rs.getString("resident_number")),
                        rs.getString("education"),
                        rs.getString("department")
                    ));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM employee")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employee (id, employee_name, grade, resident_number, education, department) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getId());
            ps.setString(2, e.getEmployeeName());
            ps.setString(3, e.getGrade());
            ps.setString(4, e.getResidentNumber());
            ps.setString(5, e.getEducation());
            ps.setString(6, e.getDepartment());
            ps.executeUpdate();
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employee SET employee_name=?, grade=?, resident_number=?, education=?, department=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEmployeeName());
            ps.setString(2, e.getGrade());
            ps.setString(3, e.getResidentNumber());
            ps.setString(4, e.getEducation());
            ps.setString(5, e.getDepartment());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employee WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
