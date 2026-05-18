<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>휴가검색</title>
    <head>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
        <link rel="stylesheet" type="text/css" href="../style/style.css">
    </head>
    <body>
        <h1>휴가검색</h1>
        <a href="../index.jsp" class="body__button--home">
            <i class="fas fa-home"></i>
        </a>
        <div class="body__div--leave">
            <form action="searchLeave.jsp" method="GET">
                <!-- 이름 검색 -->
                <input type="text" name="employee_name" placeholder="직원 명" 
                    class="form__input" value="<%= request.getParameter("employee_name") != null ? request.getParameter("employee_name") : "" %>" />
        
                <!-- 종류 선택 -->
                <select class="form__select" name="leave_type">
                    <option value="">휴가 종류</option>
                    <option value="연가" <%= "연가".equals(request.getParameter("leave_type")) ? "selected" : "" %>>연가</option>
                    <option value="병가" <%= "병가".equals(request.getParameter("leave_type")) ? "selected" : "" %>>병가</option>
                </select>

                <!-- 시작일 검색 -->
                <label for="start-date" style="font-weight: bold">시작일</label>
                <input class="form__input--date" type="date" name="start_date" value="<%= request.getParameter("start_date") != null ? request.getParameter("start_date") : "" %>" />

                <!-- 종료일 검색 -->
                <label for="end-date" style="font-weight: bold">종료일</label>
                <input class="form__input--date" type="date" name="end_date" value="<%= request.getParameter("end_date") != null ? request.getParameter("end_date") : "" %>" />
        
                <!-- 검색 버튼 -->
                <button class="form__button" type="submit">검색</button>
            </form>
        </div>
        <!-- 검색 결과 표시 -->
        <%
            String employeeName = request.getParameter("employee_name");
            String leaveType = request.getParameter("leave_type");
            String startDate = request.getParameter("start_date");
            String endDate = request.getParameter("end_date");

            if (employeeName == null && leaveType == null && startDate == null && endDate == null) {
                employeeName = "";
                leaveType = "";
                startDate = "";
                endDate = "";
            }
        %>
        <jsp:include page="showLeaveList.jsp">
            <jsp:param name="employee_name" value="<%= employeeName %>" />
            <jsp:param name="leave_type" value="<%= leaveType %>" />
            <jsp:param name="start_date" value="<%= startDate %>" />
            <jsp:param name="end_date" value="<%= endDate %>" />
        </jsp:include>
    </body>
</html>
