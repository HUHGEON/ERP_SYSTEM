<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>직원검색</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>직원검색</h1>
    <a href="../index.jsp" class="body__button--home">
        <i class="fas fa-home"></i>
    </a>
    <div class="body__div--employee">
        <form action="searchEmployee.jsp" method="GET">
            <!-- 이름 검색 -->
            <input type="text" name="employee_name" placeholder="직원 명" 
                class="form__input" value="<%= request.getParameter("employee_name") != null ? request.getParameter("employee_name") : "" %>" />
        
            <!-- 직급 선택 -->
            <select class="form__select" name="position">
                <option value="">직급 선택</option>
                <option value="사원" <%= "사원".equals(request.getParameter("position")) ? "selected" : "" %>>사원</option>
                <option value="대리" <%= "대리".equals(request.getParameter("position")) ? "selected" : "" %>>대리</option>
                <option value="과장" <%= "과장".equals(request.getParameter("position")) ? "selected" : "" %>>과장</option>
                <option value="부장" <%= "부장".equals(request.getParameter("position")) ? "selected" : "" %>>부장</option>
                <option value="이사" <%= "이사".equals(request.getParameter("position")) ? "selected" : "" %>>이사</option>
            </select>
        
            <!-- 부서 선택 -->
            <select class="form__select" name="department">
                <option value="">부서 선택</option>
                <option value="개발자" <%= "개발자".equals(request.getParameter("department")) ? "selected" : "" %>>개발자</option>
                <option value="마케팅" <%= "마케팅".equals(request.getParameter("department")) ? "selected" : "" %>>마케팅</option>
                <option value="경영관리" <%= "경영관리".equals(request.getParameter("department")) ? "selected" : "" %>>경영관리</option>
                <option value="연구개발" <%= "연구개발".equals(request.getParameter("department")) ? "selected" : "" %>>연구개발</option>
            </select>
        
            <!-- 검색 버튼 -->
            <button class="form__button" type="submit">검색</button>
        </form>
    </div>
    <!-- 검색 결과 표시 -->
    <%
        String employeeName = request.getParameter("employee_name");
        String position = request.getParameter("position");
        String department = request.getParameter("department");

        if (employeeName == null && position == null && department == null) {
            employeeName = "";
            position = "";
            department = "";
        }
    %>
    <jsp:include page="showEmployeeList.jsp">
        <jsp:param name="employee_name" value="<%= employeeName %>" />
        <jsp:param name="position" value="<%= position %>" />
        <jsp:param name="department" value="<%= department %>" />
    </jsp:include>
</body>
</html>
