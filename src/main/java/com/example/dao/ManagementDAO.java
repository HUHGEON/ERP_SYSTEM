package com.example.dao;

import com.example.model.Employee;
import com.example.model.Management;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManagementDAO {

    public List<Management> search(String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT m.id, e.employee_name, m.permission_level FROM management m JOIN employee e ON m.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }

        List<Management> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Management(rs.getInt("id"), rs.getString("employee_name"), rs.getString("permission_level")));
                }
            }
        }
        return list;
    }

    public List<Employee> getAvailableEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, employee_name FROM employee WHERE id NOT IN (SELECT id FROM management) ORDER BY employee_name")) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setEmployeeName(rs.getString("employee_name"));
                list.add(e);
            }
        }
        return list;
    }

    public void insert(Management m) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO management (id, permission_level) VALUES (?, ?)")) {
            ps.setInt(1, m.getId());
            ps.setString(2, m.getPermissionLevel());
            ps.executeUpdate();
        }
    }

    public void update(Management m) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE management SET permission_level=? WHERE id=?")) {
            ps.setString(1, m.getPermissionLevel());
            ps.setInt(2, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM management WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
