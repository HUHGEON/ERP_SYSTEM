package com.example.dao;

import com.example.model.EvaluationItem;
import com.example.model.EvaluatorSummary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationItemDAO {

    public List<EvaluationItem> search(String evaluationId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM evaluation_item WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (evaluationId != null && !evaluationId.isEmpty()) {
            sql.append(" AND evaluation_id = ?");
            try { params.add(Integer.parseInt(evaluationId)); } catch (NumberFormatException ignored) {}
        }

        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    ));
                }
            }
        }
        return list;
    }

    public List<EvaluationItem> searchByEvaluationId(int evaluationId) throws SQLException {
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM evaluation_item WHERE evaluation_id = ?")) {
            ps.setInt(1, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    ));
                }
            }
        }
        return list;
    }

    // 고객 평가 항목 (프로젝트 기준)
    public List<EvaluationItem> getByProjectCustomer(int projectId) throws SQLException {
        String sql =
            "SELECT ei.id, ei.evaluation_id, ei.rate, ei.content, ev.participation_id " +
            "FROM evaluation_item ei " +
            "JOIN evaluation ev ON ei.evaluation_id = ev.id " +
            "JOIN customer_evaluation ce ON ce.id = ev.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "WHERE pp.project_id = ? ORDER BY ev.id, ei.id";
        return fetchItemsWithParticipation(sql, projectId);
    }

    // PM 평가 항목 + PM 이름 (프로젝트 기준)
    public List<EvaluationItem> getByProjectPmWithName(int projectId) throws SQLException {
        String sql =
            "SELECT ei.id, ei.evaluation_id, ei.rate, ei.content, e2.employee_name AS pm_name, ev.participation_id " +
            "FROM evaluation_item ei " +
            "JOIN evaluation ev ON ei.evaluation_id = ev.id " +
            "JOIN pm_evaluation pe ON pe.id = ev.id " +
            "JOIN developer d ON pe.pm_id = d.id " +
            "JOIN employee e2 ON d.id = e2.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "WHERE pp.project_id = ? ORDER BY e2.employee_name, ev.id, ei.id";
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationItem item = new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    );
                    item.setEvaluatorName(rs.getString("pm_name"));
                    item.setParticipationId(rs.getInt("participation_id"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // 동료 요약 목록 (프로젝트 기준, 중복 없이, 평균 평점 포함)
    public List<EvaluatorSummary> getPartnerSummaryByProject(int projectId) throws SQLException {
        String sql =
            "SELECT pe.partner_id, e2.employee_name AS partner_name, " +
            "COUNT(ei.id) AS cnt, COALESCE(AVG(ei.rate), 0) AS avg_rate " +
            "FROM partner_evaluation pe " +
            "JOIN developer d ON pe.partner_id = d.id " +
            "JOIN employee e2 ON d.id = e2.id " +
            "JOIN evaluation ev ON pe.id = ev.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "LEFT JOIN evaluation_item ei ON ei.evaluation_id = ev.id " +
            "WHERE pp.project_id = ? " +
            "GROUP BY pe.partner_id, e2.employee_name " +
            "ORDER BY e2.employee_name";
        List<EvaluatorSummary> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new EvaluatorSummary(
                        rs.getInt("partner_id"), rs.getString("partner_name"),
                        rs.getDouble("avg_rate"), rs.getInt("cnt")
                    ));
            }
        }
        return list;
    }

    // 특정 동료의 평가 항목 (프로젝트 + 동료 ID 기준, 카테고리 포함)
    public List<EvaluationItem> getByProjectAndPartner(int projectId, int partnerId) throws SQLException {
        String sql =
            "SELECT ei.id, ei.evaluation_id, ei.rate, ei.content, ev.participation_category, ev.participation_id " +
            "FROM evaluation_item ei " +
            "JOIN evaluation ev ON ei.evaluation_id = ev.id " +
            "JOIN partner_evaluation pe ON pe.id = ev.id " +
            "JOIN project_participation pp ON ev.participation_id = pp.id " +
            "WHERE pp.project_id = ? AND pe.partner_id = ? " +
            "ORDER BY ev.participation_category, ev.id, ei.id";
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, partnerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationItem item = new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    );
                    item.setCategory(rs.getString("participation_category"));
                    item.setParticipationId(rs.getInt("participation_id"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    private List<EvaluationItem> fetchItemsWithParticipation(String sql, int projectId) throws SQLException {
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationItem item = new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    );
                    item.setParticipationId(rs.getInt("participation_id"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    private List<EvaluationItem> fetchItems(String sql, int projectId) throws SQLException {
        List<EvaluationItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new EvaluationItem(
                        rs.getInt("id"), rs.getInt("evaluation_id"),
                        rs.getDouble("rate"), rs.getString("content")
                    ));
            }
        }
        return list;
    }

    public int nextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM evaluation_item")) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    public void insert(EvaluationItem item) throws SQLException {
        String sql = "INSERT INTO evaluation_item (id, evaluation_id, rate, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getId());
            ps.setInt(2, item.getEvaluationId());
            ps.setDouble(3, item.getRate());
            ps.setString(4, item.getContent());
            ps.executeUpdate();
        }
    }

    public void update(EvaluationItem item) throws SQLException {
        String sql = "UPDATE evaluation_item SET evaluation_id=?, rate=?, content=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getEvaluationId());
            ps.setDouble(2, item.getRate());
            ps.setString(3, item.getContent());
            ps.setInt(4, item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM evaluation_item WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
