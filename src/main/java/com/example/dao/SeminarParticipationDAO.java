package com.example.dao;

import com.example.model.SeminarParticipation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeminarParticipationDAO {

    private static final String SELECT_BASE =
        "SELECT sp.id, sp.seminar_id, s.seminar_name, sp.employee_id, e.employee_name " +
        "FROM seminar_participation sp " +
        "JOIN seminar s ON sp.seminar_id = s.id " +
        "JOIN employee e ON sp.employee_id = e.id";

    private SeminarParticipation mapRow(ResultSet rs) throws SQLException {
        return new SeminarParticipation(
            rs.getInt("id"), rs.getInt("seminar_id"), rs.getString("seminar_name"),
            rs.getInt("employee_id"), rs.getString("employee_name")
        );
    }

    public List<SeminarParticipation> searchBySeminarId(int seminarId) throws SQLException {
        String sql = SELECT_BASE + " WHERE sp.seminar_id = ? ORDER BY e.employee_name";
        List<SeminarParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seminarId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<SeminarParticipation> getByEmployeeId(int employeeId) throws SQLException {
        String sql = SELECT_BASE + " WHERE sp.employee_id = ? ORDER BY s.seminar_name";
        List<SeminarParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM seminar_participation")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(SeminarParticipation sp) throws SQLException {
        String sql = "INSERT INTO seminar_participation (id, seminar_id, employee_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sp.getId());
            ps.setInt(2, sp.getSeminarId());
            ps.setInt(3, sp.getEmployeeId());
            ps.executeUpdate();
        }
    }

    public void update(SeminarParticipation sp) throws SQLException {
        String sql = "UPDATE seminar_participation SET seminar_id=?, employee_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sp.getSeminarId());
            ps.setInt(2, sp.getEmployeeId());
            ps.setInt(3, sp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM seminar_participation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
