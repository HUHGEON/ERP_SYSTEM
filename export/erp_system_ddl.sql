-- ============================================================
-- ERP System DDL
-- Generated: 2026-05-19
-- ============================================================

-- 직급
CREATE TABLE `position` (
    `id`                INT         NOT NULL COMMENT '각 직급에 부여되는 고유 ID',
    `position_name`     ENUM('사원', '대리', '과장', '부장', '이사') NOT NULL COMMENT '해당 직급의 이름',
    `salary`            INT         NOT NULL COMMENT '해당 직급에 기본으로 주어지는 연봉',
    `annual_leave_days` INT         NULL DEFAULT 0 COMMENT '해당 직급에 기본으로 주어지는 연차 수'
);

-- 직원
CREATE TABLE `employee` (
    `id`                INT         NOT NULL COMMENT '모든 직원에게 부여되는 고유 ID',
    `position_id`       INT         NOT NULL COMMENT '각 직급에 부여되는 고유 ID',
    `employee_name`     VARCHAR(50) NOT NULL COMMENT '해당 직원의 실명',
    `resident_number`   CHAR(14)    NOT NULL COMMENT '하이픈 포함 주민번호',
    `education`         VARCHAR(100) NOT NULL COMMENT '해당 사원의 최종학력',
    `department`        ENUM('마케팅', '경영관리', '연구개발', '개발자') NULL DEFAULT NULL COMMENT '해당 사원의 현재 소속 부서',
    `phone_number`      VARCHAR(13) NOT NULL COMMENT '하이픈 포함 전화번호',
    `email`             VARCHAR(200) NOT NULL COMMENT '해당 직원의 이메일 주소',
    `hire_date`         DATETIME    NOT NULL COMMENT '직원의 입사일'
);

-- 개발자
CREATE TABLE `developer` (
    `id`                INT         NOT NULL COMMENT '각 직원에게 부여되는 고유 ID',
    `tech`              VARCHAR(100) NULL DEFAULT NULL COMMENT '해당 개발자가 보유한 기술'
);

-- 경영관리
CREATE TABLE `management` (
    `id`                INT         NOT NULL COMMENT '경영관리 부서 소속 직원의 직원 ID',
    `permission_level`  VARCHAR(20) NOT NULL COMMENT '해당 직원이 접근 가능한 권한 단계'
);

-- 인사 기록 (승진 이력)
CREATE TABLE `hr_records` (
    `id`                INT         NOT NULL COMMENT '각 인사 기록에 부여되는 고유 ID',
    `employee_id`       INT         NOT NULL COMMENT '인사 평가의 대상이 되는 직원의 ID',
    `position_id`       INT         NOT NULL COMMENT '승진한 직급의 ID',
    `promotion_date`    DATE        NULL DEFAULT NULL COMMENT '해당 직원이 승진한 날짜'
);

-- 휴가기록
CREATE TABLE `leave_records` (
    `id`                INT         NOT NULL COMMENT '각 휴가에 부여되는 고유 ID',
    `employee_id`       INT         NOT NULL COMMENT '휴가를 사용한 직원의 ID',
    `leave_type`        ENUM('연가', '공가') NOT NULL COMMENT '사용한 휴가의 종류',
    `start_date`        DATE        NULL DEFAULT NULL COMMENT '휴가를 시작한 날짜',
    `end_date`          DATE        NULL DEFAULT NULL COMMENT '휴가를 종료한 날짜'
);

-- 발주처
CREATE TABLE `customer` (
    `id`                INT         NOT NULL COMMENT '발주처에 부여되는 고유 ID',
    `customer_name`     VARCHAR(100) NOT NULL COMMENT '발주처의 이름'
);

-- 프로젝트
CREATE TABLE `project` (
    `id`                INT         NOT NULL COMMENT '프로젝트에 부여되는 고유 ID',
    `customer_id`       INT         NOT NULL COMMENT '발주처에 부여되는 고유 ID',
    `project_name`      VARCHAR(100) NOT NULL COMMENT '프로젝트 이름',
    `start_date`        DATE        NOT NULL COMMENT '프로젝트 시작 일자',
    `end_date`          DATE        NULL COMMENT '프로젝트 종료 일자'
);

-- 프로젝트 투입
CREATE TABLE `project_participation` (
    `id`                INT         NOT NULL COMMENT '프로젝트 투입에 부여되는 고유 ID',
    `project_id`        INT         NOT NULL COMMENT '참여한 프로젝트를 식별하는 ID',
    `developer_id`      INT         NOT NULL COMMENT '프로젝트에 투입된 개발자의 ID',
    `project_role`      VARCHAR(50) NULL COMMENT '프로젝트에서 수행하는 역할',
    `start_date`        DATE        NOT NULL COMMENT '프로젝트 시작 일자',
    `end_date`          DATE        NULL COMMENT '프로젝트 종료 일자'
);

-- 산출물
CREATE TABLE `output` (
    `id`                INT         NOT NULL COMMENT '산출물에 부여되는 고유 ID',
    `project_id`        INT         NOT NULL COMMENT '프로젝트에 부여되는 고유 ID',
    `output_type`       VARCHAR(50) NULL COMMENT '산출물의 종류',
    `output_name`       VARCHAR(100) NULL COMMENT '산출물의 이름'
);

-- 평가
CREATE TABLE `evaluation` (
    `id`                    INT     NOT NULL COMMENT '평가에 부여되는 고유 ID',
    `participation_id`      INT     NOT NULL COMMENT '프로젝트 투입에 부여되는 고유 ID',
    `participation_category` ENUM('업무 수행', '커뮤니케이션') NULL COMMENT '평가 유형'
);

-- 평가 항목
CREATE TABLE `evaluation_item` (
    `id`                INT         NOT NULL COMMENT '평가 항목에 부여되는 고유 ID',
    `evaluation_id`     INT         NOT NULL COMMENT '평가에 부여되는 고유 ID',
    `rate`              DECIMAL(3,2) NULL COMMENT '평가 점수',
    `content`           TEXT        NULL COMMENT '평가 내용'
);

-- 고객 평가
CREATE TABLE `customer_evaluation` (
    `id`                INT         NOT NULL COMMENT '평가에 부여되는 고유 ID',
    `customer_id`       INT         NOT NULL COMMENT '발주처에 부여되는 고유 ID'
);

-- PM 평가
CREATE TABLE `pm_evaluation` (
    `id`                INT         NOT NULL COMMENT '평가에 부여되는 고유 ID',
    `pm_id`             INT         NOT NULL COMMENT 'PM 역할을 맡은 개발자의 ID'
);

-- 동료 평가
CREATE TABLE `partner_evaluation` (
    `id`                INT         NOT NULL COMMENT '평가에 부여되는 고유 ID',
    `partner_id`        INT         NOT NULL COMMENT '동료 평가에 참여한 개발자의 ID'
);

-- 경력
CREATE TABLE `career` (
    `id`                INT         NOT NULL COMMENT '특정 경력을 나타내는 고유 ID',
    `employee_id`       INT         NOT NULL COMMENT '개발 경력을 가진 개발자의 ID',
    `company_name`      VARCHAR(100) NOT NULL COMMENT '소속 회사명',
    `start_time`        DATE        NOT NULL COMMENT '입사한 날짜',
    `end_time`          DATE        NOT NULL COMMENT '퇴사한 날짜'
);

-- 세미나
CREATE TABLE `seminar` (
    `id`                INT         NOT NULL COMMENT '각 세미나에 부여되는 고유 ID',
    `seminar_name`      VARCHAR(100) NOT NULL COMMENT '해당 세미나의 이름',
    `topic`             VARCHAR(100) NULL COMMENT '해당 세미나의 주제',
    `date_time`         DATETIME    NOT NULL COMMENT '해당 세미나의 진행 일시'
);

-- 세미나 참여
CREATE TABLE `seminar_participation` (
    `id`                INT         NOT NULL COMMENT '세미나 참여 시 부여되는 고유 ID',
    `seminar_id`        INT         NOT NULL COMMENT '참여한 세미나의 ID',
    `employee_id`       INT         NOT NULL COMMENT '세미나에 참여한 직원의 ID'
);

-- 세미나 평가
CREATE TABLE `seminar_evaluation` (
    `id`                INT         NOT NULL COMMENT '각 세미나 평가에 부여되는 고유 ID',
    `seminar_id`        INT         NOT NULL COMMENT '각 세미나에 부여되는 고유 ID',
    `employee_id`       INT         NOT NULL COMMENT '세미나에 참여한 직원의 ID',
    `rating`            DECIMAL(3,2) NOT NULL COMMENT '세미나의 만족도 평가 점수',
    `comment`           VARCHAR(500) NOT NULL COMMENT '세미나 후기 작성 내용'
);

-- 스터디
CREATE TABLE `study` (
    `id`                INT         NOT NULL COMMENT '각 스터디에 부여되는 고유 ID',
    `study_name`        VARCHAR(50) NULL DEFAULT NULL COMMENT '해당 스터디의 이름',
    `category`          VARCHAR(30) NULL DEFAULT NULL COMMENT '해당 스터디의 종류'
);

-- 스터디 참여
CREATE TABLE `study_participation` (
    `id`                INT         NOT NULL COMMENT '스터디 참여 시 부여되는 고유 ID',
    `study_id`          INT         NOT NULL COMMENT '참여한 스터디의 ID',
    `employee_id`       INT         NOT NULL COMMENT '해당 스터디에 참여한 직원의 ID'
);

-- 스터디 활동 기록
CREATE TABLE `study_activity_history` (
    `id`                INT         NOT NULL COMMENT '각 스터디 활동 기록에 부여되는 고유 ID',
    `study_id`          INT         NOT NULL COMMENT '기록할 스터디의 ID',
    `activity_date`     DATE        NULL DEFAULT NULL COMMENT '스터디를 진행한 날짜',
    `content`           TEXT        NULL COMMENT '각 일자별 평가 내용'
);

-- ============================================================
-- PRIMARY KEYS
-- ============================================================

ALTER TABLE `position`              ADD CONSTRAINT `PK_POSITION`              PRIMARY KEY (`id`);
ALTER TABLE `employee`              ADD CONSTRAINT `PK_EMPLOYEE`              PRIMARY KEY (`id`);
ALTER TABLE `developer`             ADD CONSTRAINT `PK_DEVELOPER`             PRIMARY KEY (`id`);
ALTER TABLE `management`            ADD CONSTRAINT `PK_MANAGEMENT`            PRIMARY KEY (`id`);
ALTER TABLE `hr_records`            ADD CONSTRAINT `PK_HR_RECORDS`            PRIMARY KEY (`id`);
ALTER TABLE `leave_records`         ADD CONSTRAINT `PK_LEAVE_RECORDS`         PRIMARY KEY (`id`);
ALTER TABLE `customer`              ADD CONSTRAINT `PK_CUSTOMER`              PRIMARY KEY (`id`);
ALTER TABLE `project`               ADD CONSTRAINT `PK_PROJECT`               PRIMARY KEY (`id`);
ALTER TABLE `project_participation` ADD CONSTRAINT `PK_PROJECT_PARTICIPATION` PRIMARY KEY (`id`);
ALTER TABLE `output`                ADD CONSTRAINT `PK_OUTPUT`                PRIMARY KEY (`id`);
ALTER TABLE `evaluation`            ADD CONSTRAINT `PK_EVALUATION`            PRIMARY KEY (`id`);
ALTER TABLE `evaluation_item`       ADD CONSTRAINT `PK_EVALUATION_ITEM`       PRIMARY KEY (`id`);
ALTER TABLE `customer_evaluation`   ADD CONSTRAINT `PK_CUSTOMER_EVALUATION`   PRIMARY KEY (`id`);
ALTER TABLE `pm_evaluation`         ADD CONSTRAINT `PK_PM_EVALUATION`         PRIMARY KEY (`id`);
ALTER TABLE `partner_evaluation`    ADD CONSTRAINT `PK_PARTNER_EVALUATION`    PRIMARY KEY (`id`);
ALTER TABLE `career`                ADD CONSTRAINT `PK_CAREER`                PRIMARY KEY (`id`);
ALTER TABLE `seminar`               ADD CONSTRAINT `PK_SEMINAR`               PRIMARY KEY (`id`);
ALTER TABLE `seminar_participation` ADD CONSTRAINT `PK_SEMINAR_PARTICIPATION` PRIMARY KEY (`id`);
ALTER TABLE `seminar_evaluation`    ADD CONSTRAINT `PK_SEMINAR_EVALUATION`    PRIMARY KEY (`id`);
ALTER TABLE `study`                 ADD CONSTRAINT `PK_STUDY`                 PRIMARY KEY (`id`);
ALTER TABLE `study_participation`   ADD CONSTRAINT `PK_STUDY_PARTICIPATION`   PRIMARY KEY (`id`);
ALTER TABLE `study_activity_history` ADD CONSTRAINT `PK_STUDY_ACTIVITY_HISTORY` PRIMARY KEY (`id`);

-- ============================================================
-- FOREIGN KEYS
-- ============================================================

-- 직원 → 직급
ALTER TABLE `employee` ADD CONSTRAINT `FK_position_TO_employee`
    FOREIGN KEY (`position_id`) REFERENCES `position` (`id`);

-- 개발자 → 직원 (CASCADE: 직원 삭제 시 개발자 레코드 자동 삭제)
ALTER TABLE `developer` ADD CONSTRAINT `FK_employee_TO_developer`
    FOREIGN KEY (`id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 경영관리 → 직원 (CASCADE)
ALTER TABLE `management` ADD CONSTRAINT `FK_employee_TO_management`
    FOREIGN KEY (`id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 인사기록 → 직원 (CASCADE)
ALTER TABLE `hr_records` ADD CONSTRAINT `FK_employee_TO_hr_records`
    FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 인사기록 → 직급
ALTER TABLE `hr_records` ADD CONSTRAINT `FK_position_TO_hr_records`
    FOREIGN KEY (`position_id`) REFERENCES `position` (`id`);

-- 휴가기록 → 직원 (CASCADE)
ALTER TABLE `leave_records` ADD CONSTRAINT `FK_employee_TO_leave_records`
    FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 프로젝트 → 발주처
ALTER TABLE `project` ADD CONSTRAINT `FK_customer_TO_project`
    FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`);

-- 프로젝트 투입 → 프로젝트
ALTER TABLE `project_participation` ADD CONSTRAINT `FK_project_TO_project_participation`
    FOREIGN KEY (`project_id`) REFERENCES `project` (`id`);

-- 프로젝트 투입 → 개발자 (CASCADE: 개발자 삭제 시 투입 이력 자동 삭제)
ALTER TABLE `project_participation` ADD CONSTRAINT `FK_developer_TO_project_participation`
    FOREIGN KEY (`developer_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE;

-- 산출물 → 프로젝트
ALTER TABLE `output` ADD CONSTRAINT `FK_project_TO_output`
    FOREIGN KEY (`project_id`) REFERENCES `project` (`id`);

-- 평가 → 프로젝트 투입 (CASCADE)
ALTER TABLE `evaluation` ADD CONSTRAINT `FK_project_participation_TO_evaluation`
    FOREIGN KEY (`participation_id`) REFERENCES `project_participation` (`id`) ON DELETE CASCADE;

-- 평가 항목 → 평가 (CASCADE)
ALTER TABLE `evaluation_item` ADD CONSTRAINT `FK_evaluation_TO_evaluation_item`
    FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE;

-- 고객 평가 → 평가 (CASCADE)
ALTER TABLE `customer_evaluation` ADD CONSTRAINT `FK_evaluation_TO_customer_evaluation`
    FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE;

-- 고객 평가 → 발주처
ALTER TABLE `customer_evaluation` ADD CONSTRAINT `FK_customer_TO_customer_evaluation`
    FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`);

-- PM 평가 → 평가 (CASCADE)
ALTER TABLE `pm_evaluation` ADD CONSTRAINT `FK_evaluation_TO_pm_evaluation`
    FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE;

-- PM 평가 → 개발자 (CASCADE)
ALTER TABLE `pm_evaluation` ADD CONSTRAINT `FK_developer_TO_pm_evaluation`
    FOREIGN KEY (`pm_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE;

-- 동료 평가 → 평가 (CASCADE)
ALTER TABLE `partner_evaluation` ADD CONSTRAINT `FK_evaluation_TO_partner_evaluation`
    FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE;

-- 동료 평가 → 개발자 (CASCADE)
ALTER TABLE `partner_evaluation` ADD CONSTRAINT `FK_developer_TO_partner_evaluation`
    FOREIGN KEY (`partner_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE;

-- 경력 → 개발자 (CASCADE)
ALTER TABLE `career` ADD CONSTRAINT `FK_developer_TO_career`
    FOREIGN KEY (`employee_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE;

-- 세미나 참여 → 세미나
ALTER TABLE `seminar_participation` ADD CONSTRAINT `FK_seminar_TO_seminar_participation`
    FOREIGN KEY (`seminar_id`) REFERENCES `seminar` (`id`);

-- 세미나 참여 → 직원 (CASCADE)
ALTER TABLE `seminar_participation` ADD CONSTRAINT `FK_employee_TO_seminar_participation`
    FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 세미나 평가 → 세미나
ALTER TABLE `seminar_evaluation` ADD CONSTRAINT `FK_seminar_TO_seminar_evaluation`
    FOREIGN KEY (`seminar_id`) REFERENCES `seminar` (`id`);

-- 세미나 평가 → 직원 (CASCADE)
ALTER TABLE `seminar_evaluation` ADD CONSTRAINT `FK_employee_TO_seminar_evaluation`
    FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 스터디 참여 → 스터디
ALTER TABLE `study_participation` ADD CONSTRAINT `FK_study_TO_study_participation`
    FOREIGN KEY (`study_id`) REFERENCES `study` (`id`);

-- 스터디 참여 → 직원 (CASCADE)
ALTER TABLE `study_participation` ADD CONSTRAINT `FK_employee_TO_study_participation`
    FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE;

-- 스터디 활동 기록 → 스터디
ALTER TABLE `study_activity_history` ADD CONSTRAINT `FK_study_TO_study_activity_history`
    FOREIGN KEY (`study_id`) REFERENCES `study` (`id`);

-- ============================================================
-- UNIQUE CONSTRAINTS
-- ============================================================

-- 직원: 주민번호·전화번호·이메일은 인당 고유
ALTER TABLE `employee` ADD CONSTRAINT `UQ_EMPLOYEE_RESIDENT` UNIQUE (`resident_number`);
ALTER TABLE `employee` ADD CONSTRAINT `UQ_EMPLOYEE_PHONE`    UNIQUE (`phone_number`);
ALTER TABLE `employee` ADD CONSTRAINT `UQ_EMPLOYEE_EMAIL`    UNIQUE (`email`);

-- 발주처: 이름 중복 방지 (findOrCreateCustomer 전제 조건)
ALTER TABLE `customer` ADD CONSTRAINT `UQ_CUSTOMER_NAME` UNIQUE (`customer_name`);

-- 프로젝트 투입: 동일 프로젝트에 동일 개발자 중복 불가
ALTER TABLE `project_participation` ADD CONSTRAINT `UQ_PP_PROJECT_DEV` UNIQUE (`project_id`, `developer_id`);

-- 세미나 참여: 동일 세미나에 동일 직원 중복 불가
ALTER TABLE `seminar_participation` ADD CONSTRAINT `UQ_SEMINAR_PART_EMP` UNIQUE (`seminar_id`, `employee_id`);

-- 세미나 평가: 동일 세미나에 동일 직원 평가 중복 불가
ALTER TABLE `seminar_evaluation` ADD CONSTRAINT `UQ_SEMINAR_EVAL_EMP` UNIQUE (`seminar_id`, `employee_id`);

-- 스터디 참여: 동일 스터디에 동일 직원 중복 불가
ALTER TABLE `study_participation` ADD CONSTRAINT `UQ_STUDY_PART_EMP` UNIQUE (`study_id`, `employee_id`);

-- ============================================================
-- VIEW: 직원별 잔여 연차 (연가만 차감, 공가 제외)
-- ============================================================

CREATE VIEW `employee_leave_status` AS
SELECT
    e.id,
    e.employee_name,
    p.annual_leave_days,
    COALESCE(SUM(
        CASE WHEN lr.leave_type = '연가'
        THEN DATEDIFF(lr.end_date, lr.start_date) + 1
        ELSE 0 END
    ), 0) AS used_days,
    p.annual_leave_days - COALESCE(SUM(
        CASE WHEN lr.leave_type = '연가'
        THEN DATEDIFF(lr.end_date, lr.start_date) + 1
        ELSE 0 END
    ), 0) AS remaining_days
FROM `employee` e
JOIN `position` p ON e.position_id = p.id
LEFT JOIN `leave_records` lr ON e.id = lr.employee_id
    AND YEAR(lr.start_date) = YEAR(CURDATE())
GROUP BY e.id, e.employee_name, p.annual_leave_days;

-- ============================================================
-- EVENT SCHEDULER: 입사일 기준 2년마다 자동 승진
-- ============================================================

SET GLOBAL event_scheduler = ON;

CREATE EVENT `auto_promotion`
ON SCHEDULE EVERY 1 DAY
STARTS CURDATE()
DO
BEGIN
    -- 근속 연수 기준으로 예상 직급 계산 후 변경된 경우만 업데이트
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

    -- 승진된 직원 이력 기록
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
END;
