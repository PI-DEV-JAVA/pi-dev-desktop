-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Mar 01, 2026 at 09:31 PM
-- Server version: 9.1.0
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pidev`
--

-- --------------------------------------------------------

--
-- Table structure for table `activities`
--

DROP TABLE IF EXISTS `activities`;
CREATE TABLE IF NOT EXISTS `activities` (
  `id_activity` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `activity_date` date NOT NULL,
  `description` text,
  `hours_worked` decimal(5,2) DEFAULT NULL,
  `project_id` int DEFAULT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `last_activity_time` timestamp NULL DEFAULT NULL,
  `total_tracked_seconds` bigint DEFAULT '0',
  `is_tracking` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_activity`),
  KEY `fk_activity_employee` (`employee_id`),
  KEY `fk_activity_project` (`project_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `activity_files`
--

DROP TABLE IF EXISTS `activity_files`;
CREATE TABLE IF NOT EXISTS `activity_files` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `activity_tracking_history`
--

DROP TABLE IF EXISTS `activity_tracking_history`;
CREATE TABLE IF NOT EXISTS `activity_tracking_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL,
  `session_start` timestamp NOT NULL,
  `session_end` timestamp NULL DEFAULT NULL,
  `seconds_tracked` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_session_start` (`session_start`),
  KEY `idx_session_end` (`session_end`),
  KEY `idx_activity_session` (`activity_id`,`session_start`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
CREATE TABLE IF NOT EXISTS `applications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `offer_id` int NOT NULL,
  `cv_file_path` varchar(500) DEFAULT NULL,
  `motivation_letter` text,
  `status` varchar(50) DEFAULT 'Nouvelle',
  `application_date` date DEFAULT (curdate()),
  `score` double DEFAULT '0',
  `notes` text,
  `interviewer` varchar(150) DEFAULT NULL,
  `interview_date` date DEFAULT NULL,
  `interview_result` varchar(100) DEFAULT NULL,
  `recruiter_response` text,
  `response_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_offer` (`user_id`,`offer_id`),
  KEY `offer_id` (`offer_id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `applications`
--

INSERT INTO `applications` (`id`, `user_id`, `offer_id`, `cv_file_path`, `motivation_letter`, `status`, `application_date`, `score`, `notes`, `interviewer`, `interview_date`, `interview_result`, `recruiter_response`, `response_date`) VALUES
(1, 14, 2, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hey there', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 15, 2, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hey', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 14, 3, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hello', 'Acceptée', '2026-02-26', 0, NULL, NULL, NULL, NULL, 'très bien', '2026-02-26'),
(4, 17, 3, NULL, '', 'Refusée', '2026-02-26', 0, NULL, NULL, NULL, NULL, 'your profile is incomplete', '2026-02-26'),
(5, 17, 2, NULL, '', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 17, 4, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hey', 'Refusée', '2026-02-28', 0, NULL, NULL, NULL, NULL, 'BARA ZAMER', '2026-02-28'),
(7, 19, 4, NULL, 'hey there im looking forward to meet ya', 'Nouvelle', '2026-03-01', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(8, 19, 2, NULL, 'ayo whats up good fella!', 'Nouvelle', '2026-03-01', 0, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `offers`
--

DROP TABLE IF EXISTS `offers`;
CREATE TABLE IF NOT EXISTS `offers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `department` varchar(100) DEFAULT NULL,
  `contract_type` varchar(50) DEFAULT NULL,
  `experience_level` varchar(50) DEFAULT NULL,
  `salary_min` double DEFAULT NULL,
  `salary_max` double DEFAULT NULL,
  `location` varchar(150) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `publish_date` date DEFAULT NULL,
  `closing_date` date DEFAULT NULL,
  `positions_available` int DEFAULT '1',
  `applications_received` int DEFAULT '0',
  `recruiter_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `f1` (`recruiter_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `offers`
--

INSERT INTO `offers` (`id`, `title`, `description`, `department`, `contract_type`, `experience_level`, `salary_min`, `salary_max`, `location`, `status`, `publish_date`, `closing_date`, `positions_available`, `applications_received`, `recruiter_id`) VALUES
(2, 'ijio', 'fejzifjjifezjoi', 'IT', 'CDI', 'Senior', 4555, 11555, 'Tunis', 'Ouverte', '2026-02-23', '2026-03-25', 6, 5, 0),
(3, 'dev', 'hey there', 'Finance', 'CDI', 'Junior', 1000, 1666, 'tunis', 'Fermée', '2026-02-26', '2026-03-27', 12, 2, 13),
(4, 'Senior DataScientist', 'Bs or Ms in Computer Science is required for this post.\n+10 years of experience preferable.', 'IT', 'CDD', 'Senior', 6500, 7852, 'Paris', 'Ouverte', '2026-02-28', '2026-03-29', 14, 2, 13);

-- --------------------------------------------------------

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
CREATE TABLE IF NOT EXISTS `profiles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `professional_title` varchar(100) DEFAULT NULL,
  `years_of_experience` int DEFAULT NULL,
  `summary` text,
  `profile_completed` tinyint(1) DEFAULT '0',
  `profile_picture_path` varchar(500) DEFAULT NULL,
  `cv_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `profiles`
--

INSERT INTO `profiles` (`id`, `user_id`, `first_name`, `last_name`, `birth_date`, `phone_number`, `location`, `professional_title`, `years_of_experience`, `summary`, `profile_completed`, `profile_picture_path`, `cv_path`) VALUES
(1, 4, 'ayoub', 'hamed', '2000-02-17', '26413091', 'Tunis', 'Software Engineer', 2, 'hey im looking forward for your offers!!', 1, NULL, NULL),
(2, 5, 'ademv', 'hamed', '2006-04-15', '99111666', 'ariana', 'graphics designer', 3, 'hey there, let\'s connect!', 1, NULL, NULL),
(13, 16, 'ayoub', 'nma', '2002-02-07', '4644', 'aeioak', 'engineer', 4, 'hey there', 1, NULL, NULL),
(4, 7, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, NULL, NULL),
(5, 8, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, NULL, NULL),
(6, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL),
(7, 10, 'meniar', 'mili', '2004-02-18', '11222666', 'ariana', 'Engineer', 2, 'hey there im meniar!', 1, NULL, NULL),
(8, 11, 'admin', 'sudo', '2001-02-09', '95666888', 'ariana', 'admin', 1, 'hey there im sudo!', 1, NULL, NULL),
(9, 12, 'Meniar', 'Mili', '2000-02-03', '66555444', 'Ariana', 'Software Engineer', 4, 'hey there , let\'s connect !', 1, NULL, NULL),
(10, 13, 'amen', 'samader', '2000-10-22', '66555888', 'ariana', 'RH', 2, 'hey there, Im Amen , and Im so hungry and foolish!', 1, NULL, NULL),
(11, 14, 'skander', 'nafti', '2004-02-06', '99888666', 'Nkhilet, Ariana', 'Product manager', 15, 'Hey there, I\'m Nafti and I like belotte alot !', 1, NULL, NULL),
(12, 15, 'Ayoub', 'hm', '2003-02-14', '66555222', 'gafsa', 'Data Scienctist', 1, 'hey there , any welcome?', 1, NULL, NULL),
(14, 17, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '17.jpg', NULL),
(16, 19, 'ayoub', 'hamed', '2000-02-25', '95666333', 'Manhattan, New York', 'Co-founder of Talentos', 2, 'hey there,you can call me Sudo', 1, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
CREATE TABLE IF NOT EXISTS `project` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `status` enum('PLANNED','IN_PROGRESS','DONE','ON_HOLD') DEFAULT 'PLANNED',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `project_manager_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`id`, `name`, `description`, `status`, `start_date`, `end_date`, `budget`, `project_manager_id`, `created_at`) VALUES
(3, 'gta', 'gta 6 coming soon', 'PLANNED', '2026-02-27', '2026-04-03', 689.00, 13, '2026-02-27 16:46:11'),
(4, 'TEST TRELLO ', 'test maa ayoub', 'PLANNED', '2026-03-27', '2026-03-28', 10.00, 13, '2026-03-01 20:56:14'),
(5, 'trellooo', 'yo', 'PLANNED', '2026-03-24', '2026-03-31', 985.00, 13, '2026-03-01 21:30:48');

-- --------------------------------------------------------

--
-- Table structure for table `support_tickets`
--

DROP TABLE IF EXISTS `support_tickets`;
CREATE TABLE IF NOT EXISTS `support_tickets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `subject` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `category` enum('BUG','QUESTION','FEATURE','OTHER') DEFAULT 'OTHER',
  `status` enum('OPEN','IN_PROGRESS','RESOLVED','CLOSED') DEFAULT 'OPEN',
  `priority` enum('LOW','MEDIUM','HIGH') DEFAULT 'MEDIUM',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `support_tickets`
--

INSERT INTO `support_tickets` (`id`, `user_id`, `subject`, `message`, `category`, `status`, `priority`, `created_at`) VALUES
(1, 19, 'streak points', 'could you add daily points feature where user get prized for each day he connect to the app', 'FEATURE', 'CLOSED', 'MEDIUM', '2026-03-01 00:01:16');

-- --------------------------------------------------------

--
-- Table structure for table `syncs`
--

DROP TABLE IF EXISTS `syncs`;
CREATE TABLE IF NOT EXISTS `syncs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  `reason` enum('COLLABORATE','LEARN','MENTOR','HIRE','NETWORK') DEFAULT 'NETWORK',
  `status` enum('PENDING','ACCEPTED','DECLINED') DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `accepted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sender_id` (`sender_id`,`receiver_id`),
  KEY `receiver_id` (`receiver_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `syncs`
--

INSERT INTO `syncs` (`id`, `sender_id`, `receiver_id`, `reason`, `status`, `created_at`, `accepted_at`) VALUES
(1, 19, 13, 'NETWORK', 'ACCEPTED', '2026-03-01 13:32:07', '2026-03-01 13:32:50');

-- --------------------------------------------------------

--
-- Table structure for table `sync_messages`
--

DROP TABLE IF EXISTS `sync_messages`;
CREATE TABLE IF NOT EXISTS `sync_messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sync_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `sync_id` (`sync_id`),
  KEY `sender_id` (`sender_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `sync_messages`
--

INSERT INTO `sync_messages` (`id`, `sync_id`, `sender_id`, `message`, `is_read`, `created_at`) VALUES
(1, 1, 13, 'hey there , what\'s up?', 1, '2026-03-01 13:33:11'),
(2, 1, 19, 'im good how you doing?', 1, '2026-03-01 13:33:49'),
(3, 1, 19, 'yo dude', 1, '2026-03-01 17:07:21'),
(4, 1, 13, 'my bad ,i was eating', 1, '2026-03-01 17:49:39'),
(5, 1, 13, 'wanna hang out?', 1, '2026-03-01 17:49:48'),
(6, 1, 19, 'yeah sure', 0, '2026-03-01 20:07:30');

-- --------------------------------------------------------

--
-- Table structure for table `ticket_replies`
--

DROP TABLE IF EXISTS `ticket_replies`;
CREATE TABLE IF NOT EXISTS `ticket_replies` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ticket_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `ticket_id` (`ticket_id`),
  KEY `sender_id` (`sender_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `ticket_replies`
--

INSERT INTO `ticket_replies` (`id`, `ticket_id`, `sender_id`, `message`, `is_read`, `created_at`) VALUES
(1, 1, 12, 'thank you , we\'re working on it!', 1, '2026-03-01 00:03:39'),
(2, 1, 19, 'thank you', 1, '2026-03-01 00:04:11'),
(3, 1, 12, 'mrigl', 1, '2026-03-01 00:06:11');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','HR','CANDIDATE') NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `auth_provider` enum('LOCAL','GOOGLE') NOT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `email_verified` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=MyISAM AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `role`, `active`, `created_at`, `auth_provider`, `provider_id`, `email_verified`) VALUES
(1, 'ayoubhamed@esprit.tn', '62d18522b74d75b2a84776c91ba5498377441d4c4af0cea22ca7de9e09475d3a', 'CANDIDATE', 1, '2026-02-09 01:34:01', 'LOCAL', NULL, 0),
(19, 'ayoubhamed111@gmail.com', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'CANDIDATE', 1, '2026-02-27 16:13:22', 'LOCAL', NULL, 1),
(4, 'ayoub@test.com', '97da10d6a688e01e08944d2339eefb163fb5a9e066641c70f2f377f2173b36b8', 'CANDIDATE', 1, '2026-02-09 03:22:56', 'LOCAL', NULL, 0),
(5, 'ayo@test.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'ADMIN', 1, '2026-02-09 04:59:22', 'LOCAL', NULL, 0),
(16, 'ayoub@gmail.com', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'CANDIDATE', 1, '2026-02-16 14:44:22', 'LOCAL', NULL, 0),
(7, 'adem@test.com', '2ac9a6746aca543af8dff39894cfe8173afba21eb01c6fae33d52947222855ef', 'CANDIDATE', 1, '2026-02-10 13:17:11', 'LOCAL', NULL, 0),
(8, 'omar', 'f6e0a1e2ac41945a9aa7ff8a8aaa0cebc12a3bcc981a929ad5cf810a090e11ae', 'CANDIDATE', 1, '2026-02-11 17:24:04', 'LOCAL', NULL, 0),
(9, 'uai', 'f1534392279bddbf9d43dde8701cb5be14b82f76ec6607bf8d6ad557f60f304e', 'CANDIDATE', 1, '2026-02-11 17:34:49', 'LOCAL', NULL, 0),
(10, 'meniar@lll.com', 'ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb', 'CANDIDATE', 1, '2026-02-15 20:56:37', 'LOCAL', NULL, 0),
(11, 'ayo@test.fr', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'ADMIN', 0, '2026-02-15 21:21:24', 'LOCAL', NULL, 0),
(12, 'meniar@esp.tn', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'ADMIN', 1, '2026-02-15 21:50:18', 'LOCAL', NULL, 0),
(13, 'amen@disc.lol', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'HR', 1, '2026-02-15 22:23:11', 'LOCAL', NULL, 0),
(14, 'skan@nafti.tn', '380e75d7be969ac599b85be1a516618aafd679ba11922949241885802e7b37bb', 'CANDIDATE', 1, '2026-02-16 08:47:48', 'LOCAL', NULL, 0),
(15, 'ayoub@tst.tn', '5fdf54dc68d6348b46c269d4c190f407d74de4b657b3c88a6b96750d7cc3b5bd', 'CANDIDATE', 1, '2026-02-16 10:24:25', 'LOCAL', NULL, 0),
(17, 'omar@hamdi.tn', '87e5c999eb63fa472d4498109348861923598f1fe7cb36382def69071bb9df5a', 'CANDIDATE', 1, '2026-02-26 22:42:52', 'LOCAL', NULL, 0);

-- --------------------------------------------------------

--
-- Table structure for table `user_skills`
--

DROP TABLE IF EXISTS `user_skills`;
CREATE TABLE IF NOT EXISTS `user_skills` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `skill` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`skill`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_skills`
--

INSERT INTO `user_skills` (`id`, `user_id`, `skill`) VALUES
(1, 19, 'java'),
(2, 19, 'mysql'),
(3, 19, 'design');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
