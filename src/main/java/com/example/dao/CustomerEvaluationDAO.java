package com.example.dao;

import com.example.model.CustomerEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerEvaluationDAO {

    public List<CustomerEvaluation> search(String customerName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT ce.id, ce.customer_id, c.customer_name, e.participation_category " +
            "FROM customer_evaluation ce " +
            "JOIN customer c ON ce.customer_id = c.id " +
            "JOIN evaluation e ON ce.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (customerName != null && !customerName.isEmpty()) {
            sql.append(" AND c.customer_name LIKE ?");
            params.add("%" + customerName + "%");
        }

        List<CustomerEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerEvaluation(
                        rs.getInt("id"), rs.getInt("customer_id"),
                        rs.getString("customer_name"), rs.getString("participation_category")
                    ));
                }
            }
        }
        return list;
    }

    public List<CustomerEvaluation> searchByProjectId(int projectId) throws SQLException {
        String sql =
            "SELECT ce.id, ce.customer_id, c.customer_name, ev.participation_category, " +
            "COALESCE(AVG(ei.rate), 0) AS avg_rate " +
            "FROM customer_evaluation ce " +
            "JOIN customer c ON ce.customer_id = c.id " +
            "JOIN evaluation ev ON ce.id = ev.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "LEFT JOIN evaluation_item ei ON ei.evaluation_id = ev.id " +
            "WHERE pp.project_id = ? " +
            "GROUP BY ce.id, ce.customer_id, c.customer_name, ev.participation_category";
        List<CustomerEvaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerEvaluation ce = new CustomerEvaluation(
                        rs.getInt("id"), rs.getInt("customer_id"),
                        rs.getString("customer_name"), rs.getString("participation_category")
                    );
                    ce.setAvgRate(rs.getDouble("avg_rate"));
                    list.add(ce);
                }
            }
        }
        return list;
    }

    public void insert(CustomerEvaluation ce) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO customer_evaluation (id, customer_id) VALUES (?, ?)")) {
            ps.setInt(1, ce.getId());
            ps.setInt(2, ce.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void update(CustomerEvaluation ce) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE customer_evaluation SET customer_id=? WHERE id=?")) {
            ps.setInt(1, ce.getCustomerId());
            ps.setInt(2, ce.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM customer_evaluation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
