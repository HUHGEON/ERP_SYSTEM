<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>직원 세부정보</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>직원 세부정보</h1>
    <a href="javascript:void(0);" class="body__button--back" onclick="history.back()">
        <i class="fas fa-arrow-left"></i>
    </a>
    <div class="body__div--employee_details">
        <%
            String employeeId = request.getParameter("id");
            String employeeName = request.getParameter("employee_name");
    
            if (employeeId == null || employeeId.trim().isEmpty()) {
                out.println("<p>잘못된 요청입니다. 직원 ID가 누락되었습니다.</p>");
            } else {
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM employee WHERE id = ?")) {
                    
                    pstmt.setString(1, employeeId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {

                        out.println("<div class='div--employee_row'>"
                                    + "<div class='div--employee_details'>ID</div>"
                                    + "<div class='div--employee_value'>" + rs.getString("id") + "</div>"
                                    + "</div>");
                        
                        out.println("<div class='div--employee_row'>"
                                    + "<div class='div--employee_details'>이름</div>"
                                    + "<div class='div--employee_value'>" + rs.getString("employee_name") + "</div>"
                                    + "</div>");
                            
                        out.println("<div class='div--employee_row'>"
                                    + "<div class='div--employee_details'>직급</div>"
                                    + "<div class='div--employee_value'>" + rs.getString("grade") + "</div>"
                                    + "</div>");
                            
                        out.println("<div class='div--employee_row'>"
                                    + "<div class='div--employee_details'>부서</div>"
                                    + "<div class='div--employee_value'>" + rs.getString("department") + "</div>"
                                    + "</div>");
                            
                            } else {
                            out.println("<p>직원 정보를 찾을 수 없습니다.</p>");
                        }
                    }
                } catch (Exception e) {
                    // 예외를 문자열로 변환 후 출력
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    out.println("<tr><td colspan='4'>오류가 발생했습니다:<br><pre>" + sw.toString() + "</pre></td></tr>");
                }
            }
        %>
    </div>
</body>
</html>
