package com.example.dao;

import com.example.model.Output;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OutputDAO {

    public List<Output> search(String projectName, String outputType) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT o.id, o.project_id, p.project_name, o.output_type, o.output_name " +
            "FROM output o JOIN project p ON o.project_id = p.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (projectName != null && !projectName.isEmpty()) {
            sql.append(" AND p.project_name LIKE ?");
            params.add("%" + projectName + "%");
        }
        if (outputType != null && !outputType.isEmpty()) {
            sql.append(" AND o.output_type LIKE ?");
            params.add("%" + outputType + "%");
        }

        List<Output> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Output(
                        rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                        rs.getString("output_type"), rs.getString("output_name")
                    ));
                }
            }
        }
        return list;
    }

    public List<Output> searchByProjectId(int projectId) throws SQLException {
        String sql = "SELECT o.id, o.project_id, p.project_name, o.output_type, o.output_name " +
                     "FROM output o JOIN project p ON o.project_id = p.id WHERE o.project_id = ?";
        List<Output> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Output(
                        rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                        rs.getString("output_type"), rs.getString("output_name")
                    ));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM output")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Output o) throws SQLException {
        String sql = "INSERT INTO output (id, project_id, output_type, output_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getId());
            ps.setInt(2, o.getProjectId());
            ps.setString(3, o.getOutputType());
            ps.setString(4, o.getOutputName());
            ps.executeUpdate();
        }
    }

    public void update(Output o) throws SQLException {
        String sql = "UPDATE output SET project_id=?, output_type=?, output_name=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getProjectId());
            ps.setString(2, o.getOutputType());
            ps.setString(3, o.getOutputName());
            ps.setInt(4, o.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM output WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
