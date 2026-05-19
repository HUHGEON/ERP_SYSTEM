package com.example.dao;

import com.example.model.Customer;
import com.example.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public List<Project> search(String name, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.customer_id, c.customer_name, p.project_name, p.start_date, p.end_date " +
            "FROM project p JOIN customer c ON p.customer_id = c.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND p.project_name LIKE ?");
            params.add("%" + name + "%");
        }
        if ("진행중".equals(status)) {
            sql.append(" AND p.end_date IS NULL");
        } else if ("완료".equals(status)) {
            sql.append(" AND p.end_date IS NOT NULL");
        }
        sql.append(" ORDER BY p.id");

        List<Project> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("project_name"),
                        dateStr(rs, "start_date"),
                        dateStr(rs, "end_date")
                    ));
                }
            }
        }
        return list;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, customer_name FROM customer ORDER BY customer_name")) {
            while (rs.next()) {
                list.add(new Customer(rs.getInt("id"), rs.getString("customer_name")));
            }
        }
        return list;
    }

    /** 이름으로 고객을 찾고, 없으면 신규 생성 후 id 반환 */
    public int findOrCreateCustomer(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM customer WHERE customer_name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            int newId;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM customer")) {
                newId = rs.next() ? rs.getInt(1) : 1;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customer(id, customer_name) VALUES(?, ?)")) {
                ps.setInt(1, newId);
                ps.setString(2, name);
                ps.executeUpdate();
            }
            return newId;
        }
    }

    public List<Project> getAll() throws SQLException {
        return search(null, null);
    }

    public List<Project> getCompletedByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT DISTINCT p.id, p.customer_id, c.customer_name, p.project_name, p.start_date, p.end_date " +
                     "FROM project p " +
                     "JOIN customer c ON p.customer_id = c.id " +
                     "JOIN project_participation pp ON p.id = pp.project_id " +
                     "JOIN developer d ON pp.developer_id = d.id " +
                     "WHERE d.id = ? AND p.end_date IS NOT NULL";
        List<Project> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(rs.getInt("id"), rs.getInt("customer_id"), rs.getString("customer_name"),
                        rs.getString("project_name"), dateStr(rs, "start_date"), dateStr(rs, "end_date")));
                }
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM project")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(Project p) throws SQLException {
        String sql = "INSERT INTO project (id, customer_id, project_name, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setInt(2, p.getCustomerId());
            ps.setString(3, p.getProjectName());
            ps.setString(4, p.getStartDate());
            if (p.getEndDate() != null && !p.getEndDate().isEmpty()) {
                ps.setString(5, p.getEndDate());
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void update(Project p) throws SQLException {
        String sql = "UPDATE project SET customer_id=?, project_name=?, start_date=?, end_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCustomerId());
            ps.setString(2, p.getProjectName());
            ps.setString(3, p.getStartDate());
            if (p.getEndDate() != null && !p.getEndDate().isEmpty()) {
                ps.setString(4, p.getEndDate());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM project WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static String dateStr(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toString() : null;
    }
}
