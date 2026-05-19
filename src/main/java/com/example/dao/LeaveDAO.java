package com.example.dao;

import com.example.model.Employee;
import com.example.model.LeaveRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveDAO {

    public List<LeaveRecord> search(String employeeName, String leaveType) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT lr.id, lr.employee_id, e.employee_name, lr.leave_type, lr.start_date, lr.end_date " +
            "FROM leave_records lr JOIN employee e ON lr.employee_id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (employeeName != null && !employeeName.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + employeeName + "%");
        }
        if (leaveType != null && !leaveType.isEmpty()) {
            sql.append(" AND lr.leave_type = ?");
            params.add(leaveType);
        }

        List<LeaveRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LeaveRecord(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("leave_type"),
                        dateStr(rs, "start_date"),
                        dateStr(rs, "end_date")
                    ));
                }
            }
        }
        return list;
    }

    public List<LeaveRecord> getByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT lr.id, lr.employee_id, e.employee_name, lr.leave_type, lr.start_date, lr.end_date " +
                     "FROM leave_records lr JOIN employee e ON lr.employee_id = e.id WHERE lr.employee_id = ? ORDER BY lr.start_date DESC";
        List<LeaveRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LeaveRecord(rs.getInt("id"), rs.getInt("employee_id"),
                        rs.getString("employee_name"), rs.getString("leave_type"),
                        dateStr(rs, "start_date"), dateStr(rs, "end_date")));
                }
            }
        }
        return list;
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, employee_name FROM employee ORDER BY employee_name")) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setEmployeeName(rs.getString("employee_name"));
                list.add(e);
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM leave_records")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(LeaveRecord lr) throws SQLException {
        String sql = "INSERT INTO leave_records (id, employee_id, leave_type, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lr.getId());
            ps.setInt(2, lr.getEmployeeId());
            ps.setString(3, lr.getLeaveType());
            ps.setString(4, lr.getStartDate());
            ps.setString(5, lr.getEndDate());
            ps.executeUpdate();
        }
    }

    public void update(LeaveRecord lr) throws SQLException {
        String sql = "UPDATE leave_records SET employee_id=?, leave_type=?, start_date=?, end_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lr.getEmployeeId());
            ps.setString(2, lr.getLeaveType());
            ps.setString(3, lr.getStartDate());
            ps.setString(4, lr.getEndDate());
            ps.setInt(5, lr.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM leave_records WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static String dateStr(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toString() : null;
    }

    public int getRemainingLeaveDays(int employeeId) throws SQLException {
        String sql = "SELECT remaining_days FROM employee_leave_status WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("remaining_days");
            }
        }
        return 0;
    }
}
