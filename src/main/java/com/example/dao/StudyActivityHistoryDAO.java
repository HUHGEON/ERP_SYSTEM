package com.example.dao;

import com.example.model.StudyActivityHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyActivityHistoryDAO {

    public List<StudyActivityHistory> search(String studyName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT h.id, h.study_id, s.study_name, h.activity_date, h.content " +
            "FROM study_activity_history h JOIN study s ON h.study_id = s.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (studyName != null && !studyName.isEmpty()) {
            sql.append(" AND s.study_name LIKE ?");
            params.add("%" + studyName + "%");
        }

        List<StudyActivityHistory> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudyActivityHistory(
                        rs.getInt("id"), rs.getInt("study_id"), rs.getString("study_name"),
                        rs.getString("activity_date"), rs.getString("content")
                    ));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM study_activity_history")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(StudyActivityHistory h) throws SQLException {
        String sql = "INSERT INTO study_activity_history (id, study_id, activity_date, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getId());
            ps.setInt(2, h.getStudyId());
            if (h.getActivityDate() != null && !h.getActivityDate().isEmpty()) {
                ps.setString(3, h.getActivityDate());
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, h.getContent());
            ps.executeUpdate();
        }
    }

    public void update(StudyActivityHistory h) throws SQLException {
        String sql = "UPDATE study_activity_history SET study_id=?, activity_date=?, content=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getStudyId());
            if (h.getActivityDate() != null && !h.getActivityDate().isEmpty()) {
                ps.setString(2, h.getActivityDate());
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, h.getContent());
            ps.setInt(4, h.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM study_activity_history WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
