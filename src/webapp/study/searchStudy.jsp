<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>스터디 검색</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>스터디 검색</h1>
    <a href="../index.jsp" class="body__button--home">
        <i class="fas fa-home"></i>
    </a>
    <div class="body__div--study">
        <form action="searchStudy.jsp" method="GET">
            <!-- 참여 직원 검색 -->
            <input class="form__input" type="text" name="employee_name" placeholder="참여 직원" value="<%= request.getParameter("employee_name") != null ? request.getParameter("employee_name") : "" %>"/>
            
            <!-- 스터디명 검색 -->
            <input class="form__input" type="text" name="study_name" placeholder="스터디 명" value="<%= request.getParameter("study_name") != null ? request.getParameter("study_name") : "" %>"/>
    
            <!-- 스터디 카테고리 검색 -->
            <input class="form__input" type="text" name="study_category" placeholder="카테고리" value="<%= request.getParameter("study_category") != null ? request.getParameter("study_category") : "" %>"/>
    
            <!-- 검색 버튼 -->
            <button class="form__button" type="submit">검색</button>
        </form>
    </div>
    
    <!-- 검색 결과 표시 -->
    <%
        String employeeName = request.getParameter("employee_name");
        String studyName = request.getParameter("study_name");
        String studyCategory = request.getParameter("study_category");

        if (employeeName == null && studyName == null && studyCategory == null) {
            employeeName = ""; 
            studyName = ""; 
            studyCategory = ""; 
        }
    %>
    
    <jsp:include page="showStudyList.jsp">
        <jsp:param name="employee_name" value="<%= employeeName %>" />
        <jsp:param name="study_name" value="<%= studyName %>" />
        <jsp:param name="category" value="<%= studyCategory %>" />
    </jsp:include>
</body>
</html>
