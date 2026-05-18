<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>프로젝트 세부정보</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>프로젝트 세부정보</h1>
    <a href="javascript:void(0);" class="body__button--back" onclick="history.back()">
        <i class="fas fa-arrow-left"></i>
    </a>
    <div class="body__div--project_details">
        <%
            String projectId = request.getParameter("id");
    
            if (projectId == null || projectId.trim().isEmpty()) {
                out.println("<p>잘못된 요청입니다. 프로젝트 ID가 누락되었습니다.</p>");
            } else {
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT p.id, p.project_name, c.customer_name, p.start_date, p.end_date, " +
                        "CASE WHEN p.end_date IS NULL THEN '진행중' ELSE '완료' END AS status " +
                        "FROM project p " +
                        "JOIN customer c ON p.customer_id = c.id WHERE p.id = ?")) {
    
                    pstmt.setString(1, projectId);
    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>ID</div>");
                                out.println("<div class='div--project_value'>" + rs.getString("id") + "</div>");
                                out.println("</div>");
                                
                                out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>프로젝트명</div>");
                                out.println("<div class='div--project_value'>" + rs.getString("project_name") + "</div>");
                                out.println("</div>");
                                
                                out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>발주처</div>");
                                out.println("<div class='div--project_value'>" + rs.getString("customer_name") + "</div>");
                                out.println("</div>");
                                
                                out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>시작일</div>");
                                out.println("<div class='div--project_value'>" + rs.getString("start_date") + "</div>");
                                out.println("</div>");
                                
                                String endDate = rs.getString("end_date");
                                out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>종료일</div>");
                                out.println("<div class='div--project_value'>" + (endDate != null ? endDate : "진행중") + "</div>");
                                out.println("</div>");
                                
                                out.println("<div class='div--project_row'>");
                                out.println("<div class='div--project_details'>상태</div>");
                                out.println("<div class='div--project_value'>" + rs.getString("status") + "</div>");
                                out.println("</div>");
                                
                        } else {
                            out.println("<p>프로젝트 정보를 찾을 수 없습니다.</p>");
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

    <!-- 산출물 목록 추가 -->
    <div class="body__div--output_list">
        <%
            // 프로젝트 ID를 기반으로 산출물 목록을 조회
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id, output_type, output_name FROM output WHERE project_id = ?")) {

                pstmt.setString(1, projectId);

                try (ResultSet rs = pstmt.executeQuery()) {

                    out.println("<h2 style='text-align: center'>산출물 목록</h2>");
                    out.println("<table border='1' style='margin: 50px'>");
                    out.println("<tr>");
                    out.println("<th style='padding: 10px 10px 10px 10px; text-align: center;'>ID</th>");
                    out.println("<th style='padding: 10px 100px 10px 100px; text-align: center;'>산출물 유형</th>");
                    out.println("<th style='padding: 10px 100px 10px 100px; text-align: center;'>산출물명</th>");
                    out.println("</tr>");

                    boolean hasResults = false;
                    while (rs.next()) {
                        hasResults = true;
                        int outputId = rs.getInt("id");
                        String outputType = rs.getString("output_type");
                        String outputName = rs.getString("output_name");

                        out.println("<tr onclick=\"window.location.href='showOutputDetails.jsp?id=" + outputId + "'\" style='cursor: pointer;'>");
                        out.println("<td>" + outputId + "</td>");
                        out.println("<td>" + outputType + "</td>");
                        out.println("<td>" + outputName + "</td>");
                        out.println("</tr>");
                    }

                    if (!hasResults) {
                        out.println("<tr><td colspan='3'>산출물이 없습니다.</td></tr>");
                    }
                    out.println("</table>");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                out.println("<p>오류가 발생했습니다:</p><pre>" + sw.toString() + "</pre>");
            }
        %>
    </div>
</body>
</html>