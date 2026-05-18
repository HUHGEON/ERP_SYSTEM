<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<link rel="stylesheet" type="text/css" href="../style/style.css">

<table border="1">
    <tr>
        <th>스터디 ID</th>
        <th>스터디 이름</th>
        <th>카테고리</th>
    </tr>
    <%
        String employeeName = request.getParameter("employee_name");
        String studyName = request.getParameter("study_name");
        String studyCategory = request.getParameter("category");

        StringBuilder query = new StringBuilder(
            "SELECT DISTINCT(s.id), s.study_name, s.category " +
            "FROM study s " +
            "JOIN study_participation sp ON s.id = sp.study_id " +
            "JOIN employee e ON sp.employee_id = e.id " +
            "WHERE 1=1");

        if (employeeName != null && !employeeName.trim().isEmpty()) {
            query.append(" AND employee_name LIKE ?");
        }

        if (studyName != null && !studyName.trim().isEmpty()) {
            query.append(" AND study_name LIKE ?");
        }

        if (studyCategory != null && !studyCategory.trim().isEmpty()) {
            query.append(" AND category LIKE ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
                if (employeeName != null && !employeeName.trim().isEmpty()) {
                    pstmt.setString(paramIndex++, "%" + employeeName.trim() + "%");
                }

                if (studyName != null && !studyName.trim().isEmpty()) {
                    pstmt.setString(paramIndex++, "%" + studyName.trim() + "%");
                }
    
                if (studyCategory != null && !studyCategory.trim().isEmpty()) {
                    pstmt.setString(paramIndex++, "%" + studyCategory.trim() + "%");
                }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;

                    String id = rs.getString("id");
                    String sName = rs.getString("study_name");
                    String sCategory = rs.getString("category");

                    String url = "showStudyDetails.jsp?id=" + id + "&name=" + java.net.URLEncoder.encode(sName, "UTF-8") + "&category=" + java.net.URLEncoder.encode(sCategory, "UTF-8");

                    out.println("<tr onclick=\"window.location.href='" + url + "'\" style='cursor: pointer;'>");
                    out.println("<td>" + id + "</td>");
                    out.println("<td>" + sName + "</td>");
                    out.println("<td>" + sCategory + "</td>");
                    out.println("</tr>");
                }

                // 검색 결과가 없을 경우 메시지 출력
                if (!hasResults) {
                    out.println("<tr><td colspan='3'>등록된 스터디가 없습니다.</td></tr>");
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            out.println("<tr><td colspan='3'>오류가 발생했습니다:<br><pre>" + sw.toString() + "</pre></td></tr>");
        }
    %>
</table>

<!-- SELECT DISTINCT(s.id), s.study_name, s.category
FROM study s 
JOIN study_participation sp ON s.id = sp.study_id
JOIN employee e ON sp.employee_id = e.id
WHERE employee_name="윤지수"; -->