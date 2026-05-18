<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<link rel="stylesheet" type="text/css" href="../style/style.css">

<table border="1">
    <tr>
        <th>ID</th>
        <th>이름</th>
        <th>직급</th>
        <th>부서</th>
    </tr>
    <%
        String employeeName = request.getParameter("employee_name");
        String grade = request.getParameter("position");
        String department = request.getParameter("department");

        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM employee WHERE 1=1");

        if (employeeName != null && !employeeName.trim().isEmpty()) {
            sqlQuery.append(" AND employee_name LIKE ?");
        }
        if (grade != null && !grade.trim().isEmpty()) {
            sqlQuery.append(" AND grade = ?");
        }
        if (department != null && !department.trim().isEmpty()) {
            sqlQuery.append(" AND department = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

            int paramIndex = 1;

            if (employeeName != null && !employeeName.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + employeeName + "%");
            }
            if (grade != null && !grade.trim().isEmpty()) {
                pstmt.setString(paramIndex++, grade);
            }
            if (department != null && !department.trim().isEmpty()) {
                pstmt.setString(paramIndex++, department);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String id = rs.getString("id");
                    String name = rs.getString("employee_name");
                    String gradeValue = rs.getString("grade");
                    String departmentValue = rs.getString("department");
                    
                    out.println("<tr onclick=\"window.location.href='showEmployeeDetails.jsp?id=" + id + "&name=" + name + "'\" style='cursor: pointer;'>");
                    out.println("<td>" + id + "</td>");
                    out.println("<td>" + name + "</td>");
                    out.println("<td>" + gradeValue + "</td>");
                    out.println("<td>" + departmentValue + "</td>");
                    out.println("</tr>");
                }
                if (!hasResults) {
                    out.println("<tr><td colspan='4'>검색 결과가 없습니다.</td></tr>");
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
