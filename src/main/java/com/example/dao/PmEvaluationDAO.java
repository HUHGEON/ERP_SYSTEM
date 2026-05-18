package com.example.dao;

import com.example.model.PmEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PmEvaluationDAO {

    public List<PmEvaluation> search(String pmName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT pe.id, pe.pm_id, e2.employee_name AS pm_name, e.participation_category " +
            "FROM pm_evaluation pe " +
            "JOIN developer d ON pe.pm_id = d.id " +
            "JOIN employee e2 ON d.id = e2.id " +
            "JOIN evaluation e ON pe.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (pmName != null && !pmName.isEmpty()) {
            sql.append(" AND e2.employee_name LIKE ?");
            params.add("%" + pmName + "%");
        }

        List<PmEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PmEvaluation(
                        rs.getInt("id"), rs.getInt("pm_id"),
                        rs.getString("pm_name"), rs.getString("participation_category")
                    ));
                }
            }
        }
        return list;
    }

    public void insert(PmEvaluation pe) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO pm_evaluation (id, pm_id) VALUES (?, ?)")) {
            ps.setInt(1, pe.getId());
            ps.setInt(2, pe.getPmId());
            ps.executeUpdate();
        }
    }

    public void update(PmEvaluation pe) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE pm_evaluation SET pm_id=? WHERE id=?")) {
            ps.setInt(1, pe.getPmId());
            ps.setInt(2, pe.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM pm_evaluation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
