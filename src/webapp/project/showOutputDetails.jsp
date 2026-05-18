<%@ page import="java.sql.*" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>산출물 세부정보</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>산출물 세부정보</h1>
    <a href="javascript:void(0);" class="body__button--back" onclick="history.back()">
        <i class="fas fa-arrow-left"></i>
    </a>
    <div class="body__div--output_details">
        <%
            String outputId = request.getParameter("id");

            if (outputId == null || outputId.trim().isEmpty()) {
                out.println("<p>잘못된 요청입니다. 산출물 ID가 누락되었습니다.</p>");
            } else {
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT id, output_type, output_name " +
                        "FROM output WHERE id = ?")) {

                    pstmt.setString(1, outputId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            out.println("<div class='div--project_row'>");
                            out.println("<div class='div--project_details'>ID</div>");
                            out.println("<div class='div--project_value'>" + rs.getString("id") + "</div>");
                            out.println("</div>");

                            out.println("<div class='div--project_row'>");
                            out.println("<div class='div--project_details'>산출물 유형</div>");
                            out.println("<div class='div--project_value'>" + rs.getString("output_type") + "</div>");
                            out.println("</div>");

                            out.println("<div class='div--project_row'>");
                            out.println("<div class='div--project_details'>산출물명</div>");
                            out.println("<div class='div--project_value'>" + rs.getString("output_name") + "</div>");
                            out.println("</div>");
                        } else {
                            out.println("<p>산출물 정보를 찾을 수 없습니다.</p>");
                        }
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    out.println("<p>오류가 발생했습니다:</p><pre>" + sw.toString() + "</pre>");
                }
            }
        %>
    </div>
</body>
</html>