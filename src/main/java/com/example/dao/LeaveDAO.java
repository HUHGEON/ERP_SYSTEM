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
        sql.append(" ORDER BY lr.id");

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

    /**
     * 겹치는 휴가가 있으면 시작일·종료일을 min/max로 병합(UPDATE), 없으면 신규 INSERT.
     * 여러 개가 겹치면 가장 오래된 레코드 하나에 합치고 나머지는 삭제.
     */
    public void insertOrMerge(LeaveRecord lr) throws SQLException {
        String findSql =
            "SELECT id, start_date, end_date FROM leave_records " +
            "WHERE employee_id = ? AND leave_type = ? " +
            "AND start_date <= ? AND end_date >= ?";

        List<Integer> ids = new ArrayList<>();
        String mergedStart = lr.getStartDate();
        String mergedEnd   = lr.getEndDate();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setInt(1, lr.getEmployeeId());
                    ps.setString(2, lr.getLeaveType());
                    ps.setString(3, lr.getEndDate());
                    ps.setString(4, lr.getStartDate());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt("id"));
                            String s = dateStr(rs, "start_date");
                            String e = dateStr(rs, "end_date");
                            if (s != null && s.compareTo(mergedStart) < 0) mergedStart = s;
                            if (e != null && e.compareTo(mergedEnd)   > 0) mergedEnd   = e;
                        }
                    }
                }

                if (ids.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO leave_records(id,employee_id,leave_type,start_date,end_date) VALUES(?,?,?,?,?)")) {
                        ps.setInt(1, lr.getId());
                        ps.setInt(2, lr.getEmployeeId());
                        ps.setString(3, lr.getLeaveType());
                        ps.setString(4, mergedStart);
                        ps.setString(5, mergedEnd);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE leave_records SET start_date=?, end_date=? WHERE id=?")) {
                        ps.setString(1, mergedStart);
                        ps.setString(2, mergedEnd);
                        ps.setInt(3, ids.get(0));
                        ps.executeUpdate();
                    }
                    for (int i = 1; i < ids.size(); i++) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "DELETE FROM leave_records WHERE id=?")) {
                            ps.setInt(1, ids.get(i));
                            ps.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
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

    public int getTotalLeaveDays(int employeeId) throws SQLException {
        String sql = "SELECT p.annual_leave_days FROM employee e JOIN position p ON e.position_id = p.id WHERE e.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("annual_leave_days");
            }
        }
        return 0;
    }
}
