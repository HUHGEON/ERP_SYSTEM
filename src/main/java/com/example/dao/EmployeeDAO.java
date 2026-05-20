package com.example.dao;

import com.example.model.Employee;
import com.example.model.Position;
import com.example.util.MaskingUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    private static final String SELECT_BASE =
        "SELECT e.id, e.position_id, e.employee_name, p.position_name AS grade, " +
        "e.resident_number, e.education, e.department, e.phone_number, e.email, " +
        "e.hire_date, p.salary " +
        "FROM employee e JOIN position p ON e.position_id = p.id";

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getInt("id"));
        emp.setPositionId(rs.getInt("position_id"));
        emp.setEmployeeName(rs.getString("employee_name"));
        emp.setGrade(rs.getString("grade"));
        emp.setResidentNumber(MaskingUtil.maskResidentNumber(rs.getString("resident_number")));
        emp.setEducation(rs.getString("education"));
        emp.setDepartment(rs.getString("department"));
        emp.setPhoneNumber(rs.getString("phone_number"));
        emp.setEmail(rs.getString("email"));
        emp.setHireDate(rs.getString("hire_date"));
        emp.setSalary(rs.getInt("salary"));
        return emp;
    }

    public List<Employee> search(String name, String grade, String department) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (grade != null && !grade.isEmpty()) {
            sql.append(" AND p.position_name = ?");
            params.add(grade);
        }
        if (department != null && !department.isEmpty()) {
            sql.append(" AND e.department = ?");
            params.add(department);
        }

        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Employee> getByEmployeeId(int employeeId) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.id = ?";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Position> getAllPositions() throws SQLException {
        List<Position> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, position_name, salary, annual_leave_days FROM position ORDER BY id")) {
            while (rs.next()) {
                list.add(new Position(
                    rs.getInt("id"), rs.getString("position_name"),
                    rs.getInt("salary"), rs.getInt("annual_leave_days")
                ));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM employee")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employee " +
            "(id, position_id, employee_name, resident_number, education, department, phone_number, email, hire_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getId());
            ps.setInt(2, e.getPositionId());
            ps.setString(3, e.getEmployeeName());
            ps.setString(4, e.getResidentNumber());
            ps.setString(5, e.getEducation());
            ps.setString(6, e.getDepartment());
            ps.setString(7, e.getPhoneNumber());
            ps.setString(8, e.getEmail());
            ps.setString(9, e.getHireDate());
            ps.executeUpdate();
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employee SET position_id=?, employee_name=?, resident_number=?, " +
            "education=?, department=?, phone_number=?, email=?, hire_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getPositionId());
            ps.setString(2, e.getEmployeeName());
            ps.setString(3, e.getResidentNumber());
            ps.setString(4, e.getEducation());
            ps.setString(5, e.getDepartment());
            ps.setString(6, e.getPhoneNumber());
            ps.setString(7, e.getEmail());
            ps.setString(8, e.getHireDate());
            ps.setInt(9, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employee WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * 모든 직원의 position_id를 (이전 경력 일수 합산 + 입사 이후 근무일) 기준으로 일괄 재계산.
     * 앱 시작 시 호출하여 기존 데이터도 올바른 직급을 갖도록 함.
     */
    public void recalcAllPositions(List<Position> positions) throws SQLException {
        if (positions.isEmpty()) return;

        // 직원별 이전 경력 일수 합산
        String careerSql =
            "SELECT e.id, e.hire_date, " +
            "COALESCE(SUM(DATEDIFF(c.end_time, c.start_time)), 0) AS career_days " +
            "FROM employee e " +
            "LEFT JOIN career c ON e.id = c.employee_id " +
            "GROUP BY e.id, e.hire_date";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(careerSql);
             PreparedStatement upd = conn.prepareStatement(
                 "UPDATE employee SET position_id=? WHERE id=? AND position_id<>?")) {

            while (rs.next()) {
                int    empId      = rs.getInt("id");
                String hireDate   = rs.getString("hire_date");
                long   totalDays  = rs.getLong("career_days");

                try {
                    String hd = hireDate != null ? hireDate.substring(0, 10) : "";
                    long sinceDays = ChronoUnit.DAYS.between(LocalDate.parse(hd), LocalDate.now());
                    if (sinceDays > 0) totalDays += sinceDays;
                } catch (Exception ignored) {}

                Position pos = calcPositionFor(positions, totalDays);
                if (pos == null) continue;

                upd.setInt(1, pos.getId());
                upd.setInt(2, empId);
                upd.setInt(3, pos.getId());
                upd.addBatch();
            }
            upd.executeBatch();
        }
    }

    private static Position calcPositionFor(List<Position> positions, long totalDays) {
        if (positions.isEmpty()) return null;
        long years = totalDays / 365;
        int idx;
        if      (years < 2) idx = 0;
        else if (years < 4) idx = 1;
        else if (years < 6) idx = 2;
        else if (years < 8) idx = 3;
        else                idx = positions.size() - 1;
        return positions.get(Math.min(idx, positions.size() - 1));
    }
}
