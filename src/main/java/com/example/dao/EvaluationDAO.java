package com.example.dao;

import com.example.model.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO {

    public List<Evaluation> search(String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM evaluation WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isEmpty()) {
            sql.append(" AND participation_category = ?");
            params.add(category);
        }

        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Evaluation(
                        rs.getInt("id"), rs.getInt("participation_id"), rs.getString("participation_category")
                    ));
                }
            }
        }
        return list;
    }

    public List<Evaluation> searchWithDetails(String employeeName, String projectName, String category) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT ev.id, ev.participation_id, ev.participation_category, " +
            "e.employee_name, p.project_name, pp.project_role " +
            "FROM evaluation ev " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "JOIN developer d ON pp.developer_id = d.id " +
            "JOIN employee e ON d.id = e.id " +
            "JOIN project p ON pp.project_id = p.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (employeeName != null && !employeeName.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + employeeName + "%");
        }
        if (projectName != null && !projectName.isEmpty()) {
            sql.append(" AND p.project_name LIKE ?");
            params.add("%" + projectName + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND ev.participation_category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY ev.id");

        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evaluation ev = new Evaluation(
                        rs.getInt("id"), rs.getInt("participation_id"), rs.getString("participation_category")
                    );
                    ev.setEmployeeName(rs.getString("employee_name"));
                    ev.setProjectName(rs.getString("project_name"));
                    ev.setProjectRole(rs.getString("project_role"));
                    list.add(ev);
                }
            }
        }
        return list;
    }

    public List<Evaluation> getAllEvaluations() throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM evaluation ORDER BY id")) {
            while (rs.next()) {
                list.add(new Evaluation(
                    rs.getInt("id"), rs.getInt("participation_id"), rs.getString("participation_category")
                ));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM evaluation")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Evaluation e) throws SQLException {
        String sql = "INSERT INTO evaluation (id, participation_id, participation_category) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getId());
            ps.setInt(2, e.getParticipationId());
            ps.setString(3, e.getParticipationCategory());
            ps.executeUpdate();
        }
    }

    public void update(Evaluation e) throws SQLException {
        String sql = "UPDATE evaluation SET participation_id=?, participation_category=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getParticipationId());
            ps.setString(2, e.getParticipationCategory());
            ps.setInt(3, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM evaluation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
