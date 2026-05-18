package com.example.dao;

import com.example.model.Study;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyDAO {

    public List<Study> search(String name, String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM study WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND study_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        List<Study> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Study(rs.getInt("id"), rs.getString("study_name"), rs.getString("category")));
                }
            }
        }
        return list;
    }

    public List<Study> getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT DISTINCT s.id, s.study_name, s.category " +
                     "FROM study s JOIN study_participation sp ON s.id = sp.study_id " +
                     "WHERE sp.employee_id = ? ORDER BY s.study_name";
        List<Study> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Study(rs.getInt("id"), rs.getString("study_name"), rs.getString("category")));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM study")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Study s) throws SQLException {
        String sql = "INSERT INTO study (id, study_name, category) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.setString(2, s.getStudyName());
            ps.setString(3, s.getCategory());
            ps.executeUpdate();
        }
    }

    public void update(Study s) throws SQLException {
        String sql = "UPDATE study SET study_name=?, category=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStudyName());
            ps.setString(2, s.getCategory());
            ps.setInt(3, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM study WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
