<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>스터디 세부정보</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>스터디 정보</h1>
    <a href="javascript:void(0);" class="body__button--back" onclick="history.back()">
        <i class="fas fa-arrow-left"></i>
    </a>
    <%
        String studyId = request.getParameter("id");
        String studyName = request.getParameter("name");
        String studyCategory = request.getParameter("category");

        out.println("<p style='font-weight: bold; font-size: larger'>스터디 명: " + studyName + "</p>");
        out.println("<p style='font-weight: bold; font-size: larger'>카테고리: " + studyCategory + "</p>");

        if (studyId == null || studyId.trim().isEmpty()) {
            out.println("<p>잘못된 요청입니다. 스터디 ID가 누락되었습니다.</p>");
        } else {
            String query = "SELECT e.id, e.employee_name, e.grade, e.department " +
                        "FROM study_participation sp " +
                        "JOIN employee e ON sp.employee_id = e.id " +
                        "WHERE sp.study_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, studyId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean hasResults = false;

                    out.println("<table border='1'>");
                    out.println("<tr><th>직원 ID</th><th>직원 이름</th><th>직급</th><th>부서</th></tr>");

                    while (rs.next()) {
                        hasResults = true;
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("id") + "</td>");
                        out.println("<td>" + rs.getString("employee_name") + "</td>");
                        out.println("<td>" + rs.getString("grade") + "</td>");
                        out.println("<td>" + rs.getString("department") + "</td>");
                        out.println("</tr>");
                    }

                    if (!hasResults) {
                        out.println("<tr><td colspan='4'>스터디에 참여한 직원이 없습니다.</td></tr>");
                    }

                    out.println("</table>");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                out.println("<p>오류가 발생했습니다:<br><pre>" + sw.toString() + "</pre></p>");
            }
        }
    %>
</body>
</html>
