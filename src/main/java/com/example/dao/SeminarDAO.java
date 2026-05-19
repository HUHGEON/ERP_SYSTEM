package com.example.dao;

import com.example.model.Seminar;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeminarDAO {

    public List<Seminar> search(String name, String topic) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, seminar_name, topic, date_time FROM seminar WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND seminar_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (topic != null && !topic.isEmpty()) {
            sql.append(" AND topic LIKE ?");
            params.add("%" + topic + "%");
        }
        sql.append(" ORDER BY date_time DESC");

        List<Seminar> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seminar(rs.getInt("id"), rs.getString("seminar_name"),
                        rs.getString("topic"), rs.getString("date_time")));
                }
            }
        }
        return list;
    }

    public List<Seminar> getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT DISTINCT s.id, s.seminar_name, s.topic, s.date_time " +
                     "FROM seminar s JOIN seminar_participation sp ON s.id = sp.seminar_id " +
                     "WHERE sp.employee_id = ? ORDER BY s.date_time DESC";
        List<Seminar> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seminar(rs.getInt("id"), rs.getString("seminar_name"),
                        rs.getString("topic"), rs.getString("date_time")));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM seminar")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Seminar s) throws SQLException {
        String sql = "INSERT INTO seminar (id, seminar_name, topic, date_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.setString(2, s.getSeminarName());
            ps.setString(3, s.getTopic());
            ps.setString(4, s.getDateTime());
            ps.executeUpdate();
        }
    }

    public void update(Seminar s) throws SQLException {
        String sql = "UPDATE seminar SET seminar_name=?, topic=?, date_time=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSeminarName());
            ps.setString(2, s.getTopic());
            ps.setString(3, s.getDateTime());
            ps.setInt(4, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM seminar WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
