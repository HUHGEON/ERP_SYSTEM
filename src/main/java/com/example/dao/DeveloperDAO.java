package com.example.dao;

import com.example.model.Developer;
import com.example.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeveloperDAO {

    public List<Developer> search(String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT d.id, e.employee_name, d.tech FROM developer d JOIN employee e ON d.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }
        sql.append(" ORDER BY d.id");

        List<Developer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Developer(rs.getInt("id"), rs.getString("employee_name"), rs.getString("tech")));
                }
            }
        }
        return list;
    }

    public List<Developer> getAllDevelopers() throws SQLException {
        List<Developer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT d.id, e.employee_name, d.tech FROM developer d JOIN employee e ON d.id = e.id ORDER BY e.employee_name")) {
            while (rs.next()) {
                list.add(new Developer(rs.getInt("id"), rs.getString("employee_name"), rs.getString("tech")));
            }
        }
        return list;
    }

    public List<Employee> getAvailableEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, employee_name FROM employee WHERE id NOT IN (SELECT id FROM developer) ORDER BY employee_name")) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setEmployeeName(rs.getString("employee_name"));
                list.add(e);
            }
        }
        return list;
    }

    public Developer getById(int id) throws SQLException {
        String sql = "SELECT d.id, e.employee_name, d.tech FROM developer d JOIN employee e ON d.id = e.id WHERE d.id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Developer(rs.getInt("id"), rs.getString("employee_name"), rs.getString("tech"));
                }
            }
        }
        return null;
    }

    public void insert(Developer d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO developer (id, tech) VALUES (?, ?)")) {
            ps.setInt(1, d.getId());
            ps.setString(2, d.getTech());
            ps.executeUpdate();
        }
    }

    public void update(Developer d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE developer SET tech=? WHERE id=?")) {
            ps.setString(1, d.getTech());
            ps.setInt(2, d.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM developer WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
