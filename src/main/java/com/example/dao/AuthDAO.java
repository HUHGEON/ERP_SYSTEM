package com.example.dao;

import com.example.util.UserSession;

import java.sql.*;

public class AuthDAO {
    public boolean login(String name, String residentNumber) throws SQLException {
        String sql = "SELECT id, employee_name, department FROM employee WHERE employee_name = ? AND resident_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, residentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserSession.getInstance().init(
                        rs.getInt("id"),
                        rs.getString("employee_name"),
                        rs.getString("department")
                    );
                    return true;
                }
            }
        }
        return false;
    }
}
