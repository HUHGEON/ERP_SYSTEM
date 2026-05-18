<%@ page import="java.sql.*" %>
<%@ page import="com.example.dao.DatabaseConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
    <title>프로젝트 검색</title>
<head>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> 
    <link rel="stylesheet" type="text/css" href="../style/style.css">
</head>
<body>
    <h1>프로젝트 검색</h1>
    <a href="../index.jsp" class="body__button--home">
        <i class="fas fa-home"></i>
    </a>
    <div class="body__div--project">
        <form action="searchProject.jsp" method="GET">
            <!-- 프로젝트명 검색 -->
            <input type="text" name="project_name" placeholder="프로젝트 명" 
                class="form__input" value="<%= request.getParameter("project_name") != null ? request.getParameter("project_name") : "" %>" />
    
            <!-- 발주처 검색 -->
            <input type="text" name="customer_name" placeholder="발주처" 
                class="form__input" value="<%= request.getParameter("customer_name") != null ? request.getParameter("customer_name") : "" %>" />
    
            <!-- 상태 검색 -->
            <select class="form__select" name="status">
                <option value="">상태 선택</option>
                <option value="진행중" <%= "진행중".equals(request.getParameter("status")) ? "selected" : "" %>>진행중</option>
                <option value="완료" <%= "완료".equals(request.getParameter("status")) ? "selected" : "" %>>완료</option>
            </select>
    
            <!-- 검색 버튼 -->
            <button class="form__button"  type="submit">검색</button>
        </form>
    </div>

    <!-- 검색 결과 표시 -->
    <%
        String projectName = request.getParameter("project_name");
        String customerName = request.getParameter("customer_name");
        String status = request.getParameter("status");
        
        if (projectName == null && customerName == null && status == null) {
            projectName = "";
            customerName = "";
            status = "";
        }
        
    %>
    <jsp:include page="showProjectList.jsp">
        <jsp:param name="project_name" value="<%= projectName %>" />
        <jsp:param name="customer_name" value="<%= customerName %>" />
        <jsp:param name="status" value="<%= status %>" />
    </jsp:include>
</body>
</html>
