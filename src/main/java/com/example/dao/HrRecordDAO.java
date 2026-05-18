package com.example.dao;

import com.example.model.HrRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HrRecordDAO {

    public List<HrRecord> search(String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT h.id, h.employee_id, e.employee_name, h.employment_data, h.promotion_date " +
            "FROM hr_records h JOIN employee e ON h.employee_id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }

        List<HrRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrRecord(
                        rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                        rs.getString("employment_data"), rs.getString("promotion_date")
                    ));
                }
            }
        }
        return list;
    }

    public HrRecord getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT h.id, h.employee_id, e.employee_name, h.employment_data, h.promotion_date " +
                     "FROM hr_records h JOIN employee e ON h.employee_id = e.id WHERE h.employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new HrRecord(rs.getInt("id"), rs.getInt("employee_id"),
                        rs.getString("employee_name"), rs.getString("employment_data"), rs.getString("promotion_date"));
                }
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
        String sql = "INSERT INTO hr_records (id, employee_id, employment_data, promotion_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getId());
            ps.setInt(2, h.getEmployeeId());
            ps.setString(3, h.getEmploymentData());
            if (h.getPromotionDate() != null && !h.getPromotionDate().isEmpty()) {
                ps.setString(4, h.getPromotionDate());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void update(HrRecord h) throws SQLException {
        String sql = "UPDATE hr_records SET employee_id=?, employment_data=?, promotion_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getEmployeeId());
            ps.setString(2, h.getEmploymentData());
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
