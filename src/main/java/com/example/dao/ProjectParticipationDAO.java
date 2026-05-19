package com.example.dao;

import com.example.model.ProjectParticipation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectParticipationDAO {

    public List<ProjectParticipation> search(String projectName, String developerName) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT pp.id, pp.project_id, p.project_name, pp.developer_id, e.employee_name, " +
            "pp.project_role, pp.start_date, pp.end_date " +
            "FROM project_participation pp " +
            "JOIN project p ON pp.project_id = p.id " +
            "JOIN developer d ON pp.developer_id = d.id " +
            "JOIN employee e ON d.id = e.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (projectName != null && !projectName.isEmpty()) {
            sql.append(" AND p.project_name LIKE ?");
            params.add("%" + projectName + "%");
        }
        if (developerName != null && !developerName.isEmpty()) {
            sql.append(" AND e.employee_name LIKE ?");
            params.add("%" + developerName + "%");
        }

        List<ProjectParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProjectParticipation(
                        rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                        rs.getInt("developer_id"), rs.getString("employee_name"),
                        rs.getString("project_role"), dateStr(rs, "start_date"), dateStr(rs, "end_date")
                    ));
                }
            }
        }
        return list;
    }

    public List<ProjectParticipation> getByDeveloperId(int developerId) throws SQLException {
        String sql = "SELECT pp.id, pp.project_id, p.project_name, pp.developer_id, e.employee_name, " +
                     "pp.project_role, pp.start_date, pp.end_date " +
                     "FROM project_participation pp " +
                     "JOIN project p ON pp.project_id = p.id " +
                     "JOIN developer d ON pp.developer_id = d.id " +
                     "JOIN employee e ON d.id = e.id WHERE pp.developer_id = ? ORDER BY pp.start_date DESC";
        List<ProjectParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, developerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProjectParticipation(rs.getInt("id"), rs.getInt("project_id"),
                        rs.getString("project_name"), rs.getInt("developer_id"), rs.getString("employee_name"),
                        rs.getString("project_role"), dateStr(rs, "start_date"), dateStr(rs, "end_date")));
                }
            }
        }
        return list;
    }

    public List<ProjectParticipation> getByProjectId(int projectId) throws SQLException {
        String sql = "SELECT pp.id, pp.project_id, p.project_name, pp.developer_id, e.employee_name, " +
                     "pp.project_role, pp.start_date, pp.end_date " +
                     "FROM project_participation pp " +
                     "JOIN project p ON pp.project_id = p.id " +
                     "JOIN developer d ON pp.developer_id = d.id " +
                     "JOIN employee e ON d.id = e.id WHERE pp.project_id = ? ORDER BY e.employee_name";
        List<ProjectParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProjectParticipation(
                        rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                        rs.getInt("developer_id"), rs.getString("employee_name"),
                        rs.getString("project_role"), dateStr(rs, "start_date"), dateStr(rs, "end_date")));
                }
            }
        }
        return list;
    }

    public ProjectParticipation getByProjectAndEmployee(int projectId, int employeeId) throws SQLException {
        String sql = "SELECT pp.id, pp.project_id, p.project_name, pp.developer_id, e.employee_name, " +
                     "pp.project_role, pp.start_date, pp.end_date " +
                     "FROM project_participation pp " +
                     "JOIN project p ON pp.project_id = p.id " +
                     "JOIN developer d ON pp.developer_id = d.id " +
                     "JOIN employee e ON d.id = e.id " +
                     "WHERE pp.project_id = ? AND pp.developer_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProjectParticipation(
                        rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                        rs.getInt("developer_id"), rs.getString("employee_name"),
                        rs.getString("project_role"), dateStr(rs, "start_date"), dateStr(rs, "end_date"));
                }
            }
        }
        return null;
    }

    public List<ProjectParticipation> getAllParticipations() throws SQLException {
        List<ProjectParticipation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT pp.id, pp.project_id, p.project_name, pp.developer_id, e.employee_name, " +
                "pp.project_role, pp.start_date, pp.end_date " +
                "FROM project_participation pp " +
                "JOIN project p ON pp.project_id = p.id " +
                "JOIN developer d ON pp.developer_id = d.id " +
                "JOIN employee e ON d.id = e.id ORDER BY pp.id")) {
            while (rs.next()) {
                list.add(new ProjectParticipation(
                    rs.getInt("id"), rs.getInt("project_id"), rs.getString("project_name"),
                    rs.getInt("developer_id"), rs.getString("employee_name"),
                    rs.getString("project_role"), dateStr(rs, "start_date"), dateStr(rs, "end_date")
                ));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM project_participation")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(ProjectParticipation pp) throws SQLException {
        String sql = "INSERT INTO project_participation (id, project_id, developer_id, project_role, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pp.getId());
            ps.setInt(2, pp.getProjectId());
            ps.setInt(3, pp.getDeveloperId());
            ps.setString(4, pp.getProjectRole());
            ps.setString(5, pp.getStartDate());
            if (pp.getEndDate() != null && !pp.getEndDate().isEmpty()) {
                ps.setString(6, pp.getEndDate());
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public void update(ProjectParticipation pp) throws SQLException {
        String sql = "UPDATE project_participation SET project_id=?, developer_id=?, project_role=?, start_date=?, end_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pp.getProjectId());
            ps.setInt(2, pp.getDeveloperId());
            ps.setString(3, pp.getProjectRole());
            ps.setString(4, pp.getStartDate());
            if (pp.getEndDate() != null && !pp.getEndDate().isEmpty()) {
                ps.setString(5, pp.getEndDate());
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, pp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM project_participation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static String dateStr(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toString() : null;
    }
}
