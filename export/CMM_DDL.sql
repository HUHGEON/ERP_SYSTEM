CREATE DATABASE IF NOT EXISTS `cmm` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `cmm`;

SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

-- =========================================================
-- DROP in reverse dependency order
-- =========================================================
DROP TABLE IF EXISTS `study_participation`;
DROP TABLE IF EXISTS `study_activity_history`;
DROP TABLE IF EXISTS `pm_evaluation`;
DROP TABLE IF EXISTS `partner_evaluation`;
DROP TABLE IF EXISTS `customer_evaluation`;
DROP TABLE IF EXISTS `evaluation_item`;
DROP TABLE IF EXISTS `evaluation`;
DROP TABLE IF EXISTS `project_participation`;
DROP TABLE IF EXISTS `output`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `career`;
DROP TABLE IF EXISTS `management`;
DROP TABLE IF EXISTS `leave_records`;
DROP TABLE IF EXISTS `hr_records`;
DROP TABLE IF EXISTS `developer`;
DROP TABLE IF EXISTS `study`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `employee`;

-- =========================================================
-- Level 0 : root tables (no FK)
-- =========================================================

CREATE TABLE `employee` (
                            `id` int NOT NULL,
                            `employee_name` varchar(50) NOT NULL,
                            `grade` varchar(30) DEFAULT NULL,
                            `resident_number` char(14) NOT NULL,
                            `education` varchar(200) NOT NULL,
                            `department` enum('마케팅','경영관리','연구개발','개발자') DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `resident_number` (`resident_number`),
                            KEY `idx_name` (`employee_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer` (
                            `id` int NOT NULL,
                            `customer_name` varchar(100) NOT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `study` (
                         `id` int NOT NULL,
                         `study_name` varchar(50) DEFAULT NULL,
                         `category` varchar(30) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `idx_study_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 1 : depend on employee
-- =========================================================

CREATE TABLE `developer` (
                             `id` int NOT NULL,
                             `tech` varchar(100) DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             CONSTRAINT `developer_ibfk_1` FOREIGN KEY (`id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `hr_records` (
                              `id` int NOT NULL,
                              `employee_id` int NOT NULL,
                              `employment_data` date NOT NULL,
                              `promotion_date` date DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              KEY `employee_id` (`employee_id`),
                              CONSTRAINT `hr_records_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leave_records` (
                                 `id` int NOT NULL,
                                 `employee_id` int NOT NULL,
                                 `leave_type` varchar(30) DEFAULT NULL,
                                 `start_date` date DEFAULT NULL,
                                 `end_date` date DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `employee_id` (`employee_id`),
                                 CONSTRAINT `leave_records_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `management` (
                              `id` int NOT NULL,
                              `permission_level` varchar(20) NOT NULL,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `management_ibfk_1` FOREIGN KEY (`id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 2 : depend on developer / customer
-- =========================================================

CREATE TABLE `career` (
                          `id` int NOT NULL,
                          `employee_id` int DEFAULT NULL,
                          `company_name` varchar(100) NOT NULL,
                          `start_time` date NOT NULL,
                          `end_time` date NOT NULL,
                          PRIMARY KEY (`id`),
                          KEY `employee_id` (`employee_id`),
                          CONSTRAINT `career_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `project` (
                           `id` int NOT NULL,
                           `customer_id` int NOT NULL,
                           `project_name` varchar(100) NOT NULL,
                           `start_date` date NOT NULL,
                           `end_date` date DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `customer_id` (`customer_id`),
                           CONSTRAINT `project_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 3 : depend on project (+ developer)
-- =========================================================

CREATE TABLE `output` (
                          `id` int NOT NULL,
                          `project_id` int NOT NULL,
                          `output_type` varchar(50) DEFAULT NULL,
                          `output_name` varchar(100) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          KEY `idx_output_project_id` (`project_id`),
                          CONSTRAINT `output_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `project_participation` (
                                         `id` int NOT NULL,
                                         `project_id` int NOT NULL,
                                         `developer_id` int NOT NULL,
                                         `project_role` varchar(50) DEFAULT NULL,
                                         `start_date` date NOT NULL,
                                         `end_date` date DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         KEY `project_id` (`project_id`),
                                         KEY `developer_id` (`developer_id`),
                                         CONSTRAINT `project_participation_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `project_participation_ibfk_2` FOREIGN KEY (`developer_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 4 : depend on project_participation
-- =========================================================

CREATE TABLE `evaluation` (
                              `id` int NOT NULL,
                              `participation_id` int NOT NULL,
                              `participation_category` enum('업무 수행','커뮤니케이션') NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `participation_id` (`participation_id`),
                              CONSTRAINT `evaluation_ibfk_1` FOREIGN KEY (`participation_id`) REFERENCES `project_participation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 5 : depend on evaluation (+ customer / developer)
-- =========================================================

CREATE TABLE `evaluation_item` (
                                   `id` int NOT NULL,
                                   `evaluation_id` int NOT NULL,
                                   `rate` decimal(3,2) DEFAULT NULL,
                                   `content` text,
                                   PRIMARY KEY (`id`),
                                   KEY `evaluation_id` (`evaluation_id`),
                                   CONSTRAINT `evaluation_item_ibfk_1` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer_evaluation` (
                                       `id` int NOT NULL,
                                       `customer_id` int NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `customer_id` (`customer_id`),
                                       CONSTRAINT `customer_evaluation_ibfk_1` FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `customer_evaluation_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `partner_evaluation` (
                                      `id` int NOT NULL,
                                      `partner_id` int NOT NULL,
                                      PRIMARY KEY (`id`),
                                      KEY `partner_id` (`partner_id`),
                                      CONSTRAINT `partner_evaluation_ibfk_1` FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE,
                                      CONSTRAINT `partner_evaluation_ibfk_2` FOREIGN KEY (`partner_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pm_evaluation` (
                                 `id` int NOT NULL,
                                 `pm_id` int NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `pm_id` (`pm_id`),
                                 CONSTRAINT `pm_evaluation_ibfk_1` FOREIGN KEY (`id`) REFERENCES `evaluation` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `pm_evaluation_ibfk_2` FOREIGN KEY (`pm_id`) REFERENCES `developer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- Level 6 : depend on study (+ employee)
-- =========================================================

CREATE TABLE `study_activity_history` (
                                          `id` int NOT NULL,
                                          `study_id` int NOT NULL,
                                          `activity_date` date DEFAULT NULL,
                                          `content` text,
                                          PRIMARY KEY (`id`),
                                          KEY `study_id` (`study_id`),
                                          CONSTRAINT `study_activity_history_ibfk_1` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `study_participation` (
                                       `id` int NOT NULL,
                                       `study_id` int NOT NULL,
                                       `employee_id` int NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `employee_id` (`employee_id`),
                                       KEY `idx_study_id_employee_id` (`study_id`,`employee_id`),
                                       CONSTRAINT `study_participation_ibfk_1` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `study_participation_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;