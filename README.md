# DB Design Project

데이터베이스설계 팀프로젝트 — CMM(사원 관리 시스템)

## 기술 스택

- **DB**: MySQL 8.0
- **Backend**: Java (JDBC)
- **GUI**: Java Swing
- **Web**: JSP

## 데이터베이스 설정

- DB명: `cmm`
- Host: `localhost:3306`
- User: `ureca` / PW: `ureca`

스키마 및 초기 데이터는 `export/` 폴더의 SQL 파일을 사용하세요.

```sql
-- DDL (테이블 생성)
source export/CMM_DDL.sql

-- DML (초기 데이터)
source export/CMM_DML.sql
```

## ERD

![ERD](export/CMM.png)

## 주요 테이블

| 테이블 | 설명 |
|--------|------|
| employee | 사원 기본 정보 |
| developer | 개발자 정보 |
| career | 사원 경력 |
| hr_record | 인사 기록 |
| leave | 휴가 신청 |
| project | 프로젝트 |
| project_participation | 프로젝트 참여 |
| output | 프로젝트 산출물 |
| study | 스터디 |
| study_participation | 스터디 참여 |
| study_activity_history | 스터디 활동 이력 |
| evaluation | 평가 |
| evaluation_item | 평가 항목 |
| pm_evaluation | PM 평가 |
| customer_evaluation | 고객 평가 |
| partner_evaluation | 동료 평가 |
| management | 관리 정보 |
| customer | 고객사 |

## 프로젝트 구조

```
src/
├── main/java/com/example/
│   ├── dao/          # JDBC DAO 클래스
│   ├── model/        # 엔티티 모델 클래스
│   └── swing/        # Swing GUI
│       ├── Main.java
│       ├── MainFrame.java
│       ├── panel/    # 테이블별 패널
│       └── dialog/   # 입력 다이얼로그
└── webapp/           # JSP 웹 페이지
    ├── employee/
    ├── leave/
    ├── project/
    └── study/

export/               # DB 스키마 및 산출물
├── CMM_DDL.sql
├── CMM_DML.sql
└── CMM.png
```

## 실행 방법

1. MySQL에서 `cmm` 데이터베이스 생성 후 DDL/DML 실행
2. IntelliJ IDEA에서 프로젝트 열기
3. `mysql-connector-j` JAR을 classpath에 추가
4. `com.example.swing.Main` 실행
