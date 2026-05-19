# CMM ERP System

데이터베이스설계 팀프로젝트 — 사원 관리 시스템 (Java Swing + MySQL)

## 기술 스택

| 구분 | 기술 |
|------|------|
| DB | MySQL 8.0 |
| Backend | Java 17, JDBC (순수 SQL, ORM 없음) |
| GUI | Java Swing |

---

## 실행 방법

### 1. MySQL 계정 준비

```bash
mysql -u root -p
```

```sql
CREATE DATABASE cmm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ureca'@'localhost' IDENTIFIED BY 'ureca';
GRANT ALL PRIVILEGES ON cmm.* TO 'ureca'@'localhost';
FLUSH PRIVILEGES;
```

### 2. DDL 적용

```bash
mysql -u ureca -pureca cmm < erp_system_ddl.sql
```

> DDL에 `SET GLOBAL event_scheduler = ON;` 구문이 포함되어 있지만 일반 계정 권한 부족으로 자동 실행되지 않습니다.
> 아래 **[4. 자동 승진 스케줄러 등록]** 을 반드시 따로 진행하세요.

### 3. DML 적용 (테스트 데이터)

```bash
mysql -u ureca -pureca cmm < erp_system_dml.sql
```

**기본 로그인 계정**

| 이름 | 주민등록번호 | 권한 |
|------|-------------|------|
| 김경영 | 850201-1000001 | 관리자 (경영관리 부서) |
| 강개발 | 880901-1000006 | 일반 직원 |

### 4. 자동 승진 스케줄러 등록

입사일 기준 2년마다 직급을 자동으로 올리는 MySQL EVENT입니다.  
**root 계정**으로 한 번만 실행하면 됩니다.

**① EVENT SCHEDULER 활성화**

```bash
mysql -u root -p -e "SET GLOBAL event_scheduler = ON;"
```

활성화 확인:

```sql
SHOW VARIABLES LIKE 'event_scheduler';
-- Value: ON
```

> 서버 재시작 후에도 유지하려면 MySQL 설정 파일(`my.cnf` 또는 `my.ini`)에 추가:
> ```ini
> [mysqld]
> event_scheduler=ON
> ```

**② EVENT 등록**

```bash
mysql -u root -p cmm
```

MySQL 프롬프트에서 아래 쿼리를 붙여넣고 실행합니다:

```sql
DELIMITER $$

CREATE EVENT IF NOT EXISTS `auto_promotion`
ON SCHEDULE EVERY 1 DAY
STARTS CURDATE()
DO
BEGIN
    UPDATE `employee` e
    JOIN `position` p ON p.position_name = CASE
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 2  THEN '사원'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 4  THEN '대리'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 6  THEN '과장'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 8  THEN '부장'
        ELSE '이사'
    END
    SET e.position_id = p.id
    WHERE e.position_id != p.id;

    INSERT INTO `hr_records` (`employee_id`, `position_id`, `promotion_date`)
    SELECT e.id, p.id, CURDATE()
    FROM `employee` e
    JOIN `position` p ON p.position_name = CASE
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 2  THEN '사원'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 4  THEN '대리'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 6  THEN '과장'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 8  THEN '부장'
        ELSE '이사'
    END
    WHERE e.position_id = p.id
      AND TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) % 2 = 0
      AND TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) > 0
      AND DATE_FORMAT(CURDATE(), '%m-%d') = DATE_FORMAT(e.hire_date, '%m-%d');
END$$

DELIMITER ;
```

등록 확인:

```sql
SHOW EVENTS;
-- auto_promotion | Status: ENABLED
```

**승진 기준**

| 근속 연수 | 직급 |
|-----------|------|
| 0 ~ 2년 미만 | 사원 |
| 2 ~ 4년 미만 | 대리 |
| 4 ~ 6년 미만 | 과장 |
| 6 ~ 8년 미만 | 부장 |
| 8년 이상 | 이사 |

### 5. 앱 실행

```bash
chmod +x run_swing.sh
./run_swing.sh
```

> `run_swing.sh` 내 `MYSQL_JAR` 경로가 본인 환경에 맞는지 확인하세요.

---

## 프로젝트 구조

```
ERP_SYSTEM/
├── src/main/java/com/example/
│   ├── dao/              # JDBC DAO (SQL 직접 작성)
│   ├── model/            # 엔티티 모델 (POJO)
│   ├── util/             # UserSession, MaskingUtil
│   └── swing/
│       ├── Main.java
│       ├── MainFrame.java
│       ├── LoginDialog.java
│       ├── panel/        # 기능별 패널
│       └── dialog/       # 입력/수정 다이얼로그
├── src/main/resources/
│   └── application.properties   # DB 접속 정보
├── erp_system_ddl.sql    # 스키마 (테이블 + VIEW)
├── erp_system_dml.sql    # 테스트 데이터
├── run_swing.sh          # Mac/Linux 실행 스크립트
└── run_swing_win.sh.template
```

---

## DB 스키마

### 핵심 테이블

| 테이블 | 설명 |
|--------|------|
| `position` | 직급 (position_name, salary, annual_leave_days) |
| `employee` | 사원 (position FK, phone_number, email, hire_date) |
| `developer` | 개발자 (employee FK, tech 스택) |
| `management` | 경영관리 직원 — `경영관리` 부서 = 관리자 권한 |
| `hr_records` | 인사 기록 (승진 이력, position FK) |
| `leave_records` | 휴가 기록 (연가 / 공가) |
| `career` | 경력 사항 (developer FK) |

### 역량 개발

| 테이블 | 설명 |
|--------|------|
| `study` | 스터디 |
| `study_participation` | 스터디 참여 |
| `study_activity_history` | 스터디 활동 이력 |
| `seminar` | 세미나 |
| `seminar_participation` | 세미나 참여 |
| `seminar_evaluation` | 세미나 평가 (rating, comment) |

### 프로젝트 / 평가

| 테이블 | 설명 |
|--------|------|
| `customer` | 고객사 |
| `project` | 프로젝트 (customer FK) |
| `project_participation` | 프로젝트 투입 (developer FK) |
| `output` | 프로젝트 산출물 |
| `evaluation` | 평가 (participation FK) |
| `evaluation_item` | 평가 항목 (rate, content) |
| `customer_evaluation` | 고객사 평가 |
| `pm_evaluation` | PM 평가 |
| `partner_evaluation` | 동료 평가 |

### VIEW

| 뷰 | 설명 |
|----|------|
| `employee_leave_status` | 직원별 연차 부여/사용/잔여 일수 (당해 연도 기준) |

---

## 권한 구조

- **관리자**: `department = '경영관리'` 인 직원 — 전체 데이터 조회/수정 가능
- **일반 직원**: 본인 데이터만 조회 가능 (휴가, 스터디, 세미나 등)
- 로그인: `employee_name` + `resident_number` 로 인증

---

## Git Workflow & Convention

### 브랜치 전략

- 모든 작업은 Issue 기반, 브랜치명은 이슈 번호 접두어 사용
- 구조: `feat/#{이슈번호}` (예: `feat/#13`)

### 작업 프로세스

1. **Issue 발행** — 구현할 기능 또는 버그에 대한 이슈 생성
2. **브랜치 생성** — 이슈 번호 기반 로컬 브랜치 생성
3. **Draft PR 생성** — 작업 시작 시 Draft PR로 진행 상황 공유
4. **리뷰 및 머지** — 완료 후 Draft 해제 → 팀원 리뷰 → `main` 머지

### 커밋 메시지 컨벤션

```
feat #이슈번호: 기능 요약
fix #이슈번호: 버그 수정 요약
refactor #이슈번호: 리팩토링 요약
chore #이슈번호: 설정/환경 변경 요약
```
