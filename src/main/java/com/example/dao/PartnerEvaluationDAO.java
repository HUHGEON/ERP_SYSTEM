package com.example.dao;

import com.example.model.PartnerEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerEvaluationDAO {

    public List<PartnerEvaluation> search(String partnerName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT pe.id, pe.partner_id, e2.employee_name AS partner_name, e.participation_category " +
            "FROM partner_evaluation pe " +
            "JOIN developer d ON pe.partner_id = d.id " +
            "JOIN employee e2 ON d.id = e2.id " +
            "JOIN evaluation e ON pe.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (partnerName != null && !partnerName.isEmpty()) {
            sql.append(" AND e2.employee_name LIKE ?");
            params.add("%" + partnerName + "%");
        }

        List<PartnerEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PartnerEvaluation(
                        rs.getInt("id"), rs.getInt("partner_id"),
                        rs.getString("partner_name"), rs.getString("participation_category")
                    ));
                }
            }
        }
        return list;
    }

    public List<PartnerEvaluation> searchByProjectId(int projectId) throws SQLException {
        String sql =
            "SELECT pe.id, pe.partner_id, e2.employee_name AS partner_name, ev.participation_category, " +
            "COALESCE(AVG(ei.rate), 0) AS avg_rate " +
            "FROM partner_evaluation pe " +
            "JOIN developer d ON pe.partner_id = d.id " +
            "JOIN employee e2 ON d.id = e2.id " +
            "JOIN evaluation ev ON pe.id = ev.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "LEFT JOIN evaluation_item ei ON ei.evaluation_id = ev.id " +
            "WHERE pp.project_id = ? " +
            "GROUP BY pe.id, pe.partner_id, e2.employee_name, ev.participation_category";
        List<PartnerEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PartnerEvaluation pe = new PartnerEvaluation(
                        rs.getInt("id"), rs.getInt("partner_id"),
                        rs.getString("partner_name"), rs.getString("participation_category")
                    );
                    pe.setAvgRate(rs.getDouble("avg_rate"));
                    list.add(pe);
                }
            }
        }
        return list;
    }

    public void insert(PartnerEvaluation pe) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO partner_evaluation (id, partner_id) VALUES (?, ?)")) {
            ps.setInt(1, pe.getId());
            ps.setInt(2, pe.getPartnerId());
            ps.executeUpdate();
        }
    }

    public void update(PartnerEvaluation pe) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE partner_evaluation SET partner_id=? WHERE id=?")) {
            ps.setInt(1, pe.getPartnerId());
            ps.setInt(2, pe.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM partner_evaluation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
