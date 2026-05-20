# CMM ERP System

> 데이터베이스 설계 팀프로젝트 — Java Swing + MySQL 기반 사원 관리 시스템

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| GUI | Java Swing |
| DB | MySQL 8.0 |
| DB 연동 | JDBC (순수 SQL, ORM 없음) |
| 빌드/실행 | Shell Script (`run_swing.sh`) |

---

## 주요 기능

### 👤 직원 관리
- 직원 등록 (신입 / 경력직 탭 분리)
- 신입 등록 시 선택적 경력 이력 입력 → 경력일 + 입사일 기준 직급 자동 계산
- 직원 수정 / 삭제
- 직원 상세 정보 패널 — 하단 탭(프로젝트 / 휴가 / 스터디 / 경력)에서 이력 일괄 확인
- 더블클릭으로 해당 기능 페이지 이동 및 항목 자동 선택

### 🏆 직급 / 승진
- 입사일 + 이전 경력 일수 합산 기준 직급 자동 계산
- 앱 시작 시 전체 직원 직급 일괄 재계산 (기존 데이터 경력 반영)
- MySQL Event Scheduler: 매일 자정 근속 연수 기준 직급 자동 갱신 + 인사 기록 삽입

| 총 경력 연수 | 직급 |
|---|---|
| 0 ~ 2년 미만 | 사원 |
| 2 ~ 4년 미만 | 대리 |
| 4 ~ 6년 미만 | 과장 |
| 6 ~ 8년 미만 | 부장 |
| 8년 이상 | 이사 |

### 📋 인사 기록
- 승진 이력 조회 / 등록 / 수정 / 삭제

### 🏖 휴가 관리
- 휴가 신청 / 수정 / 삭제
- 연가 / 공가 구분 (연가만 연차 차감)
- `employee_leave_status` VIEW로 연차 부여 / 사용 / 잔여 일수 실시간 조회

### 🗂 프로젝트 관리
- 프로젝트 CRUD, 발주처 관리
- 프로젝트 투입 인원 / 역할 / 기간 관리
- 산출물 등록 / 수정 / 삭제
- 비관리자는 본인 참여 프로젝트만 조회

### ⭐ 프로젝트 평가
- 고객사 평가 / PM 평가 / 동료 평가
- 항목별 점수(rate) + 코멘트(content) 등록
- 평균 평점 표시 및 카드형 평가 목록

### 📚 스터디
- 스터디 CRUD, 참여 인원 관리
- 스터디 활동 기록 (날짜별 내용)
- 비관리자는 본인 참여 스터디만 조회

### 🎤 세미나
- 세미나 CRUD, 참여 직원 관리
- 세미나 평가 (별점 + 후기) 카드형 표시
- 비관리자는 본인 참여 세미나만 조회

### 🔐 권한 구조
- **관리자**: `department = '경영관리'` 직원 — 전체 데이터 조회 / CRUD 가능
- **일반 직원**: 본인 데이터만 조회, 검색창 사용 가능
- 로그인: `employee_name` + `resident_number` 인증

### 🔄 공통 UX
- 타이틀 바 **새로고침** 버튼 — 다중 인스턴스 환경에서 데이터 동기화
- 전체 테이블 정렬 지원 (`TableRowSorter`)
- 통일된 날짜 포맷 `YYYY-MM-DD`

---

## 실행 방법

### 1. MySQL 계정 및 DB 준비

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
mysql -u ureca -pureca cmm < export/erp_system_ddl.sql
```

> `SET GLOBAL event_scheduler = ON` 구문이 포함되어 있으나 일반 계정 권한 부족으로 실패할 수 있습니다.
> **[4. MySQL Event Scheduler 등록]** 을 별도로 진행하세요.

### 3. DML 적용 (테스트 데이터)

```bash
mysql -u ureca -pureca cmm < export/erp_system_dml.sql
```

**기본 로그인 계정**

| 이름 | 주민등록번호 | 권한 |
|------|-------------|------|
| 이경주 | 750104-1000001 | 관리자 (경영관리 부서) |
| 허건 | 900301-1000011 | 일반 직원 (개발자) |

### 4. MySQL Event Scheduler 등록

입사일 기준 직급을 매일 자동으로 갱신하는 MySQL EVENT입니다.
**root 계정**으로 한 번만 실행하면 됩니다.

```bash
# Event Scheduler 활성화
mysql -u root -p -e "SET GLOBAL event_scheduler = ON;"

# 활성화 확인
mysql -u root -p -e "SHOW VARIABLES LIKE 'event_scheduler';"
# Value: ON
```

> 서버 재시작 후에도 유지하려면 `my.cnf` (또는 `my.ini`) 에 추가:
> ```ini
> [mysqld]
> event_scheduler=ON
> ```

MySQL 프롬프트에서 EVENT 등록:

```sql
DELIMITER $$

CREATE EVENT IF NOT EXISTS `auto_promotion`
ON SCHEDULE EVERY 1 DAY
STARTS CURDATE()
DO
BEGIN
    UPDATE `employee` e
    JOIN `position` p ON p.position_name = CASE
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 2 THEN '사원'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 4 THEN '대리'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 6 THEN '과장'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 8 THEN '부장'
        ELSE '이사'
    END
    SET e.position_id = p.id
    WHERE e.position_id != p.id;

    INSERT INTO `hr_records` (`employee_id`, `position_id`, `promotion_date`)
    SELECT e.id, p.id, CURDATE()
    FROM `employee` e
    JOIN `position` p ON p.position_name = CASE
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 2 THEN '사원'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 4 THEN '대리'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 6 THEN '과장'
        WHEN TIMESTAMPDIFF(YEAR, e.hire_date, CURDATE()) < 8 THEN '부장'
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
├── export/
│   ├── erp_system_ddl.sql        # 스키마 (테이블 + FK + VIEW + EVENT)
│   └── erp_system_dml.sql        # 테스트 데이터 (직원 100명)
├── src/main/java/com/example/
│   ├── dao/                      # JDBC DAO (SQL 직접 작성)
│   │   ├── EmployeeDAO.java
│   │   ├── CareerDAO.java
│   │   ├── LeaveDAO.java
│   │   ├── ProjectDAO.java
│   │   ├── ProjectParticipationDAO.java
│   │   ├── HrRecordDAO.java
│   │   ├── StudyDAO.java
│   │   ├── StudyParticipationDAO.java
│   │   ├── SeminarDAO.java
│   │   ├── SeminarParticipationDAO.java
│   │   ├── SeminarEvaluationDAO.java
│   │   └── ...
│   ├── model/                    # 엔티티 모델 (POJO)
│   ├── util/
│   │   ├── UserSession.java      # 로그인 세션 싱글턴
│   │   ├── MaskingUtil.java      # 주민번호·전화번호 포맷 필터
│   │   └── DatabaseConnection.java
│   └── swing/
│       ├── Main.java
│       ├── MainFrame.java        # 사이드바, 타이틀 바, 패널 라우팅
│       ├── LoginDialog.java
│       ├── panel/                # 기능별 패널 (Refreshable 구현)
│       └── dialog/               # 입력 / 수정 다이얼로그
├── src/main/resources/
│   └── application.properties    # DB 접속 정보
├── run_swing.sh                   # Mac / Linux 실행 스크립트
└── run_swing_win.sh.template      # Windows 실행 템플릿
```

---

## DB 스키마

### 인사 관리

| 테이블 | 설명 |
|--------|------|
| `position` | 직급 (직급명, 연봉, 연차 일수) |
| `employee` | 직원 (position FK, 부서, 입사일 등) |
| `developer` | 개발자 (employee FK, 기술스택) |
| `management` | 경영관리 직원 (employee FK, 권한 단계) |
| `hr_records` | 인사 기록 (승진 이력) |
| `career` | 경력 사항 (이전 직장 기간) |
| `leave_records` | 휴가 기록 (연가 / 공가) |

### 프로젝트 / 평가

| 테이블 | 설명 |
|--------|------|
| `customer` | 발주처 |
| `project` | 프로젝트 |
| `project_participation` | 프로젝트 투입 (developer FK) |
| `output` | 프로젝트 산출물 |
| `evaluation` | 평가 (participation FK) |
| `evaluation_item` | 평가 항목 (점수 + 내용) |
| `customer_evaluation` | 고객사 평가 |
| `pm_evaluation` | PM 평가 |
| `partner_evaluation` | 동료 평가 |

### 역량 개발

| 테이블 | 설명 |
|--------|------|
| `study` | 스터디 |
| `study_participation` | 스터디 참여 |
| `study_activity_history` | 스터디 활동 기록 |
| `seminar` | 세미나 |
| `seminar_participation` | 세미나 참여 |
| `seminar_evaluation` | 세미나 평가 (별점 + 후기) |

### VIEW

| 뷰 | 설명 |
|----|------|
| `employee_leave_status` | 직원별 연차 부여 / 사용 / 잔여 일수 (당해 연도, 연가만 차감) |

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
