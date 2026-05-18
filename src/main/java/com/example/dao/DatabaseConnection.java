package com.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // MySQL 데이터베이스 연결 정보
    private static final String URL = "jdbc:mysql://localhost:3306/cmm";
    private static final String USERNAME = "ureca";
    private static final String PASSWORD = "ureca";

    // 데이터베이스 연결을 반환하는 메서드
    public static Connection getConnection() throws SQLException {
        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 데이터베이스 연결 반환
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            // JDBC 드라이버가 없는 경우
            throw new SQLException("JDBC Driver not found", e);
        } catch (SQLException e) {
            // 데이터베이스 연결 실패 시 오류 메시지 출력
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
}