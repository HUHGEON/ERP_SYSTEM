package com.example.dao;

import com.example.model.EvaluationItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationItemDAO {

    public List<EvaluationItem> search(String evaluationId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM evaluation_item WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (evaluationId != null && !evaluationId.isEmpty()) {
            sql.append(" AND evaluation_id = ?");
            try { params.add(Integer.parseInt(evaluationId)); } catch (NumberFormatException ignored) {}
        }

        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    ));
                }
            }
        }
        return list;
    }

    public List<EvaluationItem> searchByEvaluationId(int evaluationId) throws SQLException {
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM evaluation_item WHERE evaluation_id = ?")) {
            ps.setInt(1, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    ));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM evaluation_item")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(EvaluationItem item) throws SQLException {
        String sql = "INSERT INTO evaluation_item (id, evaluation_id, rate, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getId());
            ps.setInt(2, item.getEvaluationId());
            ps.setDouble(3, item.getRate());
            ps.setString(4, item.getContent());
            ps.executeUpdate();
        }
    }

    public void update(EvaluationItem item) throws SQLException {
        String sql = "UPDATE evaluation_item SET evaluation_id=?, rate=?, content=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getEvaluationId());
            ps.setDouble(2, item.getRate());
            ps.setString(3, item.getContent());
            ps.setInt(4, item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM evaluation_item WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
