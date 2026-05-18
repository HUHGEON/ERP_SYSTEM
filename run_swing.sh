#!/bin/bash
# MySQL JDBC 드라이버 경로
MYSQL_JAR="/Users/heogeon/Downloads/apache-tomcat-10.1.24/lib/mysql-connector-j-9.1.0.jar"

SRC_DIR="src/main/java"
RESOURCES_DIR="src/main/resources"
OUT_DIR="out"

mkdir -p "$OUT_DIR"

echo "컴파일 중..."
find "$SRC_DIR" -name "*.java" | xargs javac -encoding UTF-8 -cp "$MYSQL_JAR" -d "$OUT_DIR"

if [ $? -ne 0 ]; then
    echo "컴파일 실패"
    exit 1
fi

echo "실행 중..."
java -cp "$OUT_DIR:$RESOURCES_DIR:$MYSQL_JAR" com.example.swing.Main
