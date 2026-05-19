package com.example.dao;

import com.example.model.SeminarEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeminarEvaluationDAO {

    private static final String SELECT_BASE =
        "SELECT se.id, se.seminar_id, s.seminar_name, se.employee_id, e.employee_name, se.rating, se.comment " +
        "FROM seminar_evaluation se " +
        "JOIN seminar s ON se.seminar_id = s.id " +
        "JOIN employee e ON se.employee_id = e.id";

    private SeminarEvaluation mapRow(ResultSet rs) throws SQLException {
        return new SeminarEvaluation(
            rs.getInt("id"), rs.getInt("seminar_id"), rs.getString("seminar_name"),
            rs.getInt("employee_id"), rs.getString("employee_name"),
            rs.getDouble("rating"), rs.getString("comment")
        );
    }

    public List<SeminarEvaluation> getBySeminarId(int seminarId) throws SQLException {
        String sql = SELECT_BASE + " WHERE se.seminar_id = ? ORDER BY e.employee_name";
        List<SeminarEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seminarId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM seminar_evaluation")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(SeminarEvaluation se) throws SQLException {
        String sql = "INSERT INTO seminar_evaluation (id, seminar_id, employee_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, se.getId());
            ps.setInt(2, se.getSeminarId());
            ps.setInt(3, se.getEmployeeId());
            ps.setDouble(4, se.getRating());
            ps.setString(5, se.getComment());
            ps.executeUpdate();
        }
    }

    public void update(SeminarEvaluation se) throws SQLException {
        String sql = "UPDATE seminar_evaluation SET seminar_id=?, employee_id=?, rating=?, comment=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, se.getSeminarId());
            ps.setInt(2, se.getEmployeeId());
            ps.setDouble(3, se.getRating());
            ps.setString(4, se.getComment());
            ps.setInt(5, se.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM seminar_evaluation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
