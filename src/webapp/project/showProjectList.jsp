<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<link rel="stylesheet" type="text/css" href="../style/style.css">

<table border="1">
    <tr>
        <th>ID</th>
        <th>프로젝트명</th>
        <th>발주처</th>
        <th>상태</th>
    </tr>
    <%
        String projectName = request.getParameter("project_name");
        String customerName = request.getParameter("customer_name");
        String status = request.getParameter("status");

        // 동적 SQL 쿼리 생성
        StringBuilder sqlQuery = new StringBuilder(
            "SELECT p.id, p.project_name, c.customer_name, " +
            "CASE WHEN p.end_date IS NULL THEN '진행중' ELSE '완료' END AS status " +
            "FROM project p JOIN customer c ON p.customer_id = c.id WHERE 1=1");

        if (projectName != null && !projectName.trim().isEmpty()) {
            sqlQuery.append(" AND p.project_name LIKE ?");
        }
        if (customerName != null && !customerName.trim().isEmpty()) {
            sqlQuery.append(" AND c.customer_name LIKE ?");
        }
        if (status != null && !status.trim().isEmpty()) {
            if (status.equals("진행중")) {
                sqlQuery.append(" AND p.end_date IS NULL");
            } else if (status.equals("완료")) {
                sqlQuery.append(" AND p.end_date IS NOT NULL");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

            int paramIndex = 1;

            // 동적 파라미터 설정
            if (projectName != null && !projectName.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + projectName + "%");
            }
            if (customerName != null && !customerName.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + customerName + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String id = rs.getString("id");
                    String pName = rs.getString("project_name");
                    String cName = rs.getString("customer_name");
                    String projectStatus = rs.getString("status");
                    
                    out.println("<tr onclick=\"window.location.href='showProjectDetails.jsp?id=" + id + "'\" style='cursor: pointer;'>");
                    out.println("<td>" + id + "</td>");
                    out.println("<td>" + pName + "</td>");
                    out.println("<td>" + cName + "</td>");
                    out.println("<td>" + projectStatus + "</td>");
                    out.println("</tr>");
                }
                if (!hasResults) {
                    out.println("<tr><td colspan='4'>검색 결과가 없습니다</td></tr>");
                }
            }
        } catch (Exception e) {
            // 예외를 문자열로 변환 후 출력
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            out.println("<tr><td colspan='4'>오류가 발생했습니다:<br><pre>" + sw.toString() + "</pre></td></tr>");
        }
    %>
</table>