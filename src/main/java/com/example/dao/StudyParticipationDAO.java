package com.example.dao;

import com.example.model.StudyParticipation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyParticipationDAO {

    public List<StudyParticipation> search(String studyName, String employeeName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT sp.id, sp.study_id, s.study_name, sp.employee_id, e.employee_name " +
            "FROM study_participation sp " +
            "JOIN study s ON sp.study_id = s.id " +
            "JOIN employee e ON sp.employee_id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (studyName != null && !studyName.isEmpty()) {
            sql.append(" AND s.study_name LIKE ?");
            params.add("%" + studyName + "%");
        }
        if (employeeName != null && !employeeName.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + employeeName + "%");
        }

        List<StudyParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyParticipation(
                        rs.getInt("id"), rs.getInt("study_id"), rs.getString("study_name"),
                        rs.getInt("employee_id"), rs.getString("employee_name")
                    ));
                }
            }
        }
        return list;
    }

    public List<StudyParticipation> getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT sp.id, sp.study_id, s.study_name, sp.employee_id, e.employee_name " +
                     "FROM study_participation sp " +
                     "JOIN study s ON sp.study_id = s.id " +
                     "JOIN employee e ON sp.employee_id = e.id WHERE sp.employee_id = ? ORDER BY s.study_name";
        List<StudyParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyParticipation(rs.getInt("id"), rs.getInt("study_id"),
                        rs.getString("study_name"), rs.getInt("employee_id"), rs.getString("employee_name")));
                }
            }
        }
        return list;
    }

    public List<StudyParticipation> searchByStudyId(int studyId) throws SQLException {
        String sql = "SELECT sp.id, sp.study_id, s.study_name, sp.employee_id, e.employee_name " +
                     "FROM study_participation sp " +
                     "JOIN study s ON sp.study_id = s.id " +
                     "JOIN employee e ON sp.employee_id = e.id WHERE sp.study_id = ?";
        List<StudyParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyParticipation(
                        rs.getInt("id"), rs.getInt("study_id"), rs.getString("study_name"),
                        rs.getInt("employee_id"), rs.getString("employee_name")
                    ));
                }
            }
        }
        return list;
    }

    public boolean existsByStudyAndEmployee(int studyId, int employeeId, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM study_participation WHERE study_id=? AND employee_id=? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studyId); ps.setInt(2, employeeId); ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        }
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM study_participation")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(StudyParticipation sp) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO study_participation (id, study_id, employee_id) VALUES (?, ?, ?)")) {
            ps.setInt(1, sp.getId());
            ps.setInt(2, sp.getStudyId());
            ps.setInt(3, sp.getEmployeeId());
            ps.executeUpdate();
        }
    }

    public void update(StudyParticipation sp) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE study_participation SET study_id=?, employee_id=? WHERE id=?")) {
            ps.setInt(1, sp.getStudyId());
            ps.setInt(2, sp.getEmployeeId());
            ps.setInt(3, sp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM study_participation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
