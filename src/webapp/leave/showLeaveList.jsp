<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<link rel="stylesheet" type="text/css" href="../style/style.css">

<table border="1">
    <tr>
        <th>휴가 ID</th>
        <th>직원 이름</th>
        <th>휴가 종류</th>
        <th>시작일</th>
        <th>종료일</th>
    </tr>
    <%
        String employeeName = request.getParameter("employee_name");
        String leaveType = request.getParameter("leave_type");
        String startDate = request.getParameter("start_date");
        String endDate = request.getParameter("end_date");

        StringBuilder sqlQuery = new StringBuilder(
            "SELECT lr.id, e.employee_name, lr.leave_type, lr.start_date, lr.end_date " +
            "FROM leave_records lr " +
            "JOIN employee e ON lr.employee_id = e.id " +
            "WHERE 1=1"
        );

        // 동적 조건 추가
        if (employeeName != null && !employeeName.trim().isEmpty()) {
            sqlQuery.append(" AND e.employee_name LIKE ?");
        }
        if (leaveType != null && !leaveType.trim().isEmpty()) {
            sqlQuery.append(" AND lr.leave_type = ?");
        }
        if (startDate != null && !startDate.isEmpty()) {
            sqlQuery.append(" AND lr.start_date >= ?");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sqlQuery.append(" AND lr.end_date <= ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString())) {

            int paramIndex = 1;

            // 동적 파라미터 설정
            if (employeeName != null && !employeeName.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + employeeName + "%");
            }
            if (leaveType != null && !leaveType.trim().isEmpty()) {
                pstmt.setString(paramIndex++, leaveType);
            }
            if (startDate != null && !startDate.isEmpty()) {
                pstmt.setString(paramIndex++, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                pstmt.setString(paramIndex++, endDate);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String leaveId = rs.getString("id");
                    String name = rs.getString("employee_name");
                    String leaveTypeValue = rs.getString("leave_type");
                    String startDateValue = rs.getString("start_date");
                    String endDateValue = rs.getString("end_date");

                    out.println("<tr>");
                    out.println("<td>" + leaveId + "</td>");
                    out.println("<td>" + name + "</td>");
                    out.println("<td>" + leaveTypeValue + "</td>");
                    out.println("<td>" + startDateValue + "</td>");
                    out.println("<td>" + endDateValue + "</td>");
                    out.println("</tr>");
                }
                if (!hasResults) {
                    out.println("<tr><td colspan='5'>검색 결과가 없습니다.</td></tr>");
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // 사용자에게는 간단한 오류 메시지 제공
            out.println("<tr><td colspan='5'>오류가 발생했습니다. 관리자에게 문의하세요.</td></tr>");
            // 서버 로그에 상세 오류 메시지 기록
            System.err.println(sw.toString());
        }
    %>
</table>
