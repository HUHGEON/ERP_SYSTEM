package com.example.dao;

import com.example.model.HrRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HrRecordDAO {

    private static final String SELECT_BASE =
        "SELECT h.id, h.employee_id, e.employee_name, h.position_id, p.position_name, h.promotion_date " +
        "FROM hr_records h " +
        "JOIN employee e ON h.employee_id = e.id " +
        "JOIN position p ON h.position_id = p.id";

    private HrRecord mapRow(ResultSet rs) throws SQLException {
        HrRecord h = new HrRecord(
            rs.getInt("id"), rs.getInt("employee_id"),
            rs.getString("employee_name"), rs.getInt("position_id"),
            rs.getString("promotion_date")
        );
        h.setPositionName(rs.getString("position_name"));
        return h;
    }

    public List<HrRecord> search(String name) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }
        sql.append(" ORDER BY h.promotion_date DESC");

        List<HrRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public HrRecord getByEmployeeId(int employeeId) throws SQLException {
        String sql = SELECT_BASE + " WHERE h.employee_id = ? ORDER BY h.promotion_date DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM hr_records")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(HrRecord h) throws SQLException {
        String sql = "INSERT INTO hr_records (id, employee_id, position_id, promotion_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getId());
            ps.setInt(2, h.getEmployeeId());
            ps.setInt(3, h.getPositionId());
            if (h.getPromotionDate() != null && !h.getPromotionDate().isEmpty()) {
                ps.setString(4, h.getPromotionDate());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void update(HrRecord h) throws SQLException {
        String sql = "UPDATE hr_records SET employee_id=?, position_id=?, promotion_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getEmployeeId());
            ps.setInt(2, h.getPositionId());
            if (h.getPromotionDate() != null && !h.getPromotionDate().isEmpty()) {
                ps.setString(3, h.getPromotionDate());
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, h.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM hr_records WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
