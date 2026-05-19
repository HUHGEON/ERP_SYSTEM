package com.example.dao;

import com.example.model.Career;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareerDAO {

    public List<Career> search(String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT c.id, c.employee_id, e.employee_name, c.company_name, c.start_time, c.end_time " +
            "FROM career c JOIN employee e ON c.employee_id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }
        sql.append(" ORDER BY c.id");

        List<Career> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Career(
                        rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                        rs.getString("company_name"), dateStr(rs, "start_time"), dateStr(rs, "end_time")
                    ));
                }
            }
        }
        return list;
    }

    public List<Career> getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT c.id, c.employee_id, e.employee_name, c.company_name, c.start_time, c.end_time " +
                     "FROM career c JOIN employee e ON c.employee_id = e.id WHERE c.employee_id = ? ORDER BY c.start_time DESC";
        List<Career> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Career(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                        rs.getString("company_name"), dateStr(rs, "start_time"), dateStr(rs, "end_time")));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM career")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Career c) throws SQLException {
        String sql = "INSERT INTO career (id, employee_id, company_name, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setInt(2, c.getEmployeeId());
            ps.setString(3, c.getCompanyName());
            ps.setString(4, c.getStartTime());
            ps.setString(5, c.getEndTime());
            ps.executeUpdate();
        }
    }

    public void update(Career c) throws SQLException {
        String sql = "UPDATE career SET employee_id=?, company_name=?, start_time=?, end_time=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getEmployeeId());
            ps.setString(2, c.getCompanyName());
            ps.setString(3, c.getStartTime());
            ps.setString(4, c.getEndTime());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM career WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static String dateStr(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toString() : null;
    }
}
