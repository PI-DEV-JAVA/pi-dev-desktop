-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Mar 02, 2026 at 10:21 AM
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
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `activities`
--

INSERT INTO `activities` (`id_activity`, `employee_id`, `activity_date`, `description`, `hours_worked`, `project_id`, `start_time`, `last_activity_time`, `total_tracked_seconds`, `is_tracking`) VALUES
(1, 14, '2026-03-17', '[✅ REVIEWED]\n151561', 15.00, 6, NULL, '2026-03-01 21:40:05', 345, 0),
(2, 14, '2026-03-17', '15651', 6.00, 6, NULL, NULL, 0, 0),
(3, 19, '2026-03-26', '11111', 555.00, 7, NULL, '2026-03-02 04:01:45', 18, 0),
(4, 19, '2026-04-01', '15151', 4.00, 7, NULL, NULL, 0, 0);

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
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `activity_tracking_history`
--

INSERT INTO `activity_tracking_history` (`id`, `activity_id`, `session_start`, `session_end`, `seconds_tracked`, `created_at`) VALUES
(1, 1, '2026-03-01 20:48:02', '2026-03-01 20:48:33', 30, '2026-03-01 21:48:32'),
(2, 1, '2026-03-01 20:48:33', '2026-03-01 20:48:39', 6, '2026-03-01 21:48:39'),
(3, 1, '2026-03-01 21:34:55', '2026-03-01 21:35:25', 30, '2026-03-01 22:35:24'),
(4, 1, '2026-03-01 21:35:25', '2026-03-01 21:35:55', 30, '2026-03-01 22:35:54'),
(5, 1, '2026-03-01 21:35:55', '2026-03-01 21:36:25', 30, '2026-03-01 22:36:24'),
(6, 1, '2026-03-01 21:36:25', '2026-03-01 21:36:55', 30, '2026-03-01 22:36:54'),
(7, 1, '2026-03-01 21:36:55', '2026-03-01 21:37:25', 30, '2026-03-01 22:37:24'),
(8, 1, '2026-03-01 21:37:25', '2026-03-01 21:37:55', 30, '2026-03-01 22:37:54'),
(9, 1, '2026-03-01 21:37:55', '2026-03-01 21:38:25', 30, '2026-03-01 22:38:24'),
(10, 1, '2026-03-01 21:38:25', '2026-03-01 21:38:55', 30, '2026-03-01 22:38:54'),
(11, 1, '2026-03-01 21:38:55', '2026-03-01 21:39:25', 30, '2026-03-01 22:39:24'),
(12, 1, '2026-03-01 21:39:25', '2026-03-01 21:39:55', 30, '2026-03-01 22:39:54'),
(13, 1, '2026-03-01 21:39:55', '2026-03-01 21:40:05', 9, '2026-03-01 22:40:04'),
(14, 3, '2026-03-02 04:01:26', '2026-03-02 04:01:45', 18, '2026-03-02 05:01:44');

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
(7, 19, 4, NULL, 'hey there im looking forward to meet ya', 'Acceptée', '2026-03-01', 0, NULL, NULL, NULL, NULL, 'Félicitations ! Votre candidature a été acceptée.', '2026-03-02'),
(8, 19, 2, NULL, 'ayo whats up good fella!', 'Nouvelle', '2026-03-01', 0, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `bookmarks`
--

DROP TABLE IF EXISTS `bookmarks`;
CREATE TABLE IF NOT EXISTS `bookmarks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `candidate_id` int NOT NULL,
  `offer_id` int NOT NULL,
  `saved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `notes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_bookmark` (`candidate_id`,`offer_id`),
  KEY `idx_bookmarks_candidate` (`candidate_id`),
  KEY `idx_bookmarks_offer` (`offer_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `bookmarks`
--

INSERT INTO `bookmarks` (`id`, `candidate_id`, `offer_id`, `saved_at`, `notes`) VALUES
(3, 14, 4, '2026-03-01 22:37:12', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `choix`
--

DROP TABLE IF EXISTS `choix`;
CREATE TABLE IF NOT EXISTS `choix` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL,
  `texte` text NOT NULL,
  `est_correct` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_choix_question` (`question_id`)
) ENGINE=MyISAM AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `choix`
--

INSERT INTO `choix` (`id`, `question_id`, `texte`, `est_correct`, `created_at`) VALUES
(1, 1, '2621', 1, '2026-03-02 06:26:00'),
(2, 1, '45', 0, '2026-03-02 06:26:00'),
(3, 1, '5', 0, '2026-03-02 06:26:00'),
(4, 1, '616', 0, '2026-03-02 06:26:00'),
(5, 2, 'Une variable peut être modifiée plusieurs fois après sa création, tandis qu\"une constante ne peut pas.', 1, '2026-03-02 08:07:39'),
(6, 2, 'Une variable est utilisée pour stocker des valeurs fixes, tandis que une constante peut changer.', 0, '2026-03-02 08:07:39'),
(7, 2, 'Une variable est définie avec la syntaxe var nomVariable = valeur, tandis qu\"une constante est définie avec la syntaxe let ou const nomConstante = valeur.', 0, '2026-03-02 08:07:39'),
(8, 2, 'Une variable est créée automatiquement lorsqu\"elle est utilisée pour la première fois, tandis qu\"une constante doit être déclarée avant d\"être utilisée.', 0, '2026-03-02 08:07:39'),
(9, 3, 'String, Number, Boolean, Array, Object', 1, '2026-03-02 08:07:39'),
(10, 3, 'Number, String, Boolean, Function, Object', 0, '2026-03-02 08:07:39'),
(11, 3, 'Boolean, Null, Undefined, Function, Object', 0, '2026-03-02 08:07:39'),
(12, 3, 'String, Number, Boolean, Null, Undefined', 0, '2026-03-02 08:07:39'),
(13, 4, 'Number', 0, '2026-03-02 08:07:39'),
(14, 4, 'String', 1, '2026-03-02 08:07:39'),
(15, 4, 'Boolean', 0, '2026-03-02 08:07:39'),
(16, 4, 'Array', 0, '2026-03-02 08:07:39'),
(17, 5, 'Une fonction est appelée directement depuis le code principal, tandis qu\"une méthode est appelée via un objet.', 1, '2026-03-02 08:07:39'),
(18, 5, 'Une fonction est utilisée pour manipuler les données, tandis qu\"une méthode est utilisée pour exécuter des opérations spécifiques.', 0, '2026-03-02 08:07:39'),
(19, 5, 'Une fonction est définie avec la syntaxe function nomFonction(), tandis qu\"une méthode est définie avec la syntaxe this.nomMethode()', 0, '2026-03-02 08:07:39'),
(20, 5, 'Une fonction est utilisée pour créer des objets, tandis qu\"une méthode est utilisée pour modifier les propriétés existantes.', 0, '2026-03-02 08:07:39'),
(21, 6, 'String, Number, Boolean, Array, Object', 1, '2026-03-02 08:07:39'),
(22, 6, 'Number, String, Boolean, Function, Object', 0, '2026-03-02 08:07:39'),
(23, 6, 'Boolean, Null, Undefined, Function, Object', 0, '2026-03-02 08:07:39'),
(24, 6, 'String, Number, Boolean, Null, Undefined', 0, '2026-03-02 08:07:39');

-- --------------------------------------------------------

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
CREATE TABLE IF NOT EXISTS `event` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `event_type` enum('MEETUP','CONFERENCE','WORKSHOP','WEBINAR') DEFAULT 'MEETUP',
  `event_date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `location` varchar(500) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `is_online` tinyint(1) DEFAULT '0',
  `online_link` varchar(500) DEFAULT NULL,
  `max_capacity` int DEFAULT '0',
  `cover_image` varchar(500) DEFAULT NULL,
  `organizer_id` int NOT NULL,
  `status` enum('UPCOMING','ONGOING','COMPLETED','CANCELLED') DEFAULT 'UPCOMING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `organizer_id` (`organizer_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `event_comment`
--

DROP TABLE IF EXISTS `event_comment`;
CREATE TABLE IF NOT EXISTS `event_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_id` int NOT NULL,
  `user_id` int NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `event_id` (`event_id`),
  KEY `user_id` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `event_like`
--

DROP TABLE IF EXISTS `event_like`;
CREATE TABLE IF NOT EXISTS `event_like` (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_id` int NOT NULL,
  `user_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_like` (`event_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `event_participation`
--

DROP TABLE IF EXISTS `event_participation`;
CREATE TABLE IF NOT EXISTS `event_participation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` enum('CONFIRMED','PENDING','CANCELLED','ATTENDED') DEFAULT 'PENDING',
  `registered_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `qr_code` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_participation` (`event_id`,`user_id`),
  KEY `user_id` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `formation`
--

DROP TABLE IF EXISTS `formation`;
CREATE TABLE IF NOT EXISTS `formation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(120) NOT NULL,
  `description` text,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `contenu` longtext,
  `difficulte` enum('DEBUTANT','INTERMEDIAIRE','AVANCE') NOT NULL,
  `categorie` varchar(80) NOT NULL,
  `mode` enum('EN_LIGNE','PRESENTIEL','HYBRIDE') DEFAULT 'EN_LIGNE',
  `lieu` varchar(120) DEFAULT NULL,
  `formateur` varchar(120) DEFAULT NULL,
  `prerequis` text,
  `capacite_max` int DEFAULT '0',
  `statut` enum('OUVERTE','EN_COURS','TERMINEE') NOT NULL,
  `recruiter_id` int DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_formation_recruiter` (`recruiter_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `formation`
--

INSERT INTO `formation` (`id`, `nom`, `description`, `date_debut`, `date_fin`, `contenu`, `difficulte`, `categorie`, `mode`, `lieu`, `formateur`, `prerequis`, `capacite_max`, `statut`, `recruiter_id`, `created_at`) VALUES
(1, 'gta', 'hello', '2026-03-18', '2026-03-19', 'hello world', 'DEBUTANT', 'dev', 'EN_LIGNE', '', 'ayoub', 'hah', 12, 'OUVERTE', 13, '2026-03-02 06:24:34');

-- --------------------------------------------------------

--
-- Table structure for table `inscription`
--

DROP TABLE IF EXISTS `inscription`;
CREATE TABLE IF NOT EXISTS `inscription` (
  `id` int NOT NULL AUTO_INCREMENT,
  `formation_id` int NOT NULL,
  `user_id` int NOT NULL,
  `candidat_nom` varchar(120) DEFAULT NULL,
  `candidat_email` varchar(120) DEFAULT NULL,
  `date_inscription` datetime DEFAULT CURRENT_TIMESTAMP,
  `statut` varchar(30) DEFAULT 'EN_ATTENTE',
  `score_quiz` double DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_insc_formation` (`formation_id`),
  KEY `fk_insc_user` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `inscription`
--

INSERT INTO `inscription` (`id`, `formation_id`, `user_id`, `candidat_nom`, `candidat_email`, `date_inscription`, `statut`, `score_quiz`, `created_at`) VALUES
(1, 1, 19, 'ayoubhamed111@gmail.com', 'ayoubhamed111@gmail.com', '2026-03-02 07:27:43', 'ACCEPTEE', NULL, '2026-03-02 07:27:43');

-- --------------------------------------------------------

--
-- Table structure for table `interview`
--

DROP TABLE IF EXISTS `interview`;
CREATE TABLE IF NOT EXISTS `interview` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `recruiter_id` bigint NOT NULL,
  `candidate_id` bigint NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
  `general_grade` decimal(4,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_interview_recruiter` (`recruiter_id`),
  KEY `fk_interview_candidate` (`candidate_id`),
  KEY `idx_interview_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `interview`
--

INSERT INTO `interview` (`id`, `title`, `recruiter_id`, `candidate_id`, `status`, `general_grade`, `created_at`) VALUES
(1, 'descf', 13, 14, 'PENDING', 2.00, '2026-03-02 04:29:25');

-- --------------------------------------------------------

--
-- Table structure for table `interview_meet`
--

DROP TABLE IF EXISTS `interview_meet`;
CREATE TABLE IF NOT EXISTS `interview_meet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `interview_id` bigint NOT NULL,
  `meet_uuid` char(36) NOT NULL,
  `scheduled_at` datetime NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
  `grade` decimal(4,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `meet_uuid` (`meet_uuid`),
  KEY `fk_meet_interview` (`interview_id`),
  KEY `idx_meet_status` (`status`),
  KEY `idx_meet_schedule` (`scheduled_at`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `interview_meet`
--

INSERT INTO `interview_meet` (`id`, `interview_id`, `meet_uuid`, `scheduled_at`, `status`, `grade`, `created_at`) VALUES
(1, 1, 'bAKviltC', '2026-03-03 09:30:00', 'PENDING', 0.00, '2026-03-02 04:30:07');

-- --------------------------------------------------------

--
-- Table structure for table `interview_note`
--

DROP TABLE IF EXISTS `interview_note`;
CREATE TABLE IF NOT EXISTS `interview_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `interview_id` bigint NOT NULL,
  `recruiter_id` bigint NOT NULL,
  `note` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_note_interview` (`interview_id`),
  KEY `fk_note_recruiter` (`recruiter_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `offers`
--

INSERT INTO `offers` (`id`, `title`, `description`, `department`, `contract_type`, `experience_level`, `salary_min`, `salary_max`, `location`, `status`, `publish_date`, `closing_date`, `positions_available`, `applications_received`, `recruiter_id`) VALUES
(2, 'ijio', 'fejzifjjifezjoi', 'IT', 'CDI', 'Senior', 4555, 11555, 'Tunis', 'Ouverte', '2026-02-23', '2026-03-25', 6, 5, 0),
(3, 'dev', 'hey there', 'Finance', 'CDI', 'Junior', 1000, 1666, 'tunis', 'Fermée', '2026-02-26', '2026-03-27', 12, 2, 13),
(4, 'Senior DataScientist', 'Bs or Ms in Computer Science is required for this post.\n+10 years of experience preferable.', 'IT', 'CDD', 'Senior', 6500, 7852, 'Paris', 'Ouverte', '2026-02-28', '2026-03-29', 14, 2, 13),
(5, 'graphics designer', 'hey there pls apply!', 'IT', 'CDI', 'Intermédiaire', 10000, 25555, 'Paris', 'Ouverte', '2026-03-02', '2026-04-02', 11, 0, 13);

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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`id`, `name`, `description`, `status`, `start_date`, `end_date`, `budget`, `project_manager_id`, `created_at`) VALUES
(3, 'gta', 'gta 6 coming soon', 'PLANNED', '2026-02-27', '2026-04-03', 689.00, 13, '2026-02-27 16:46:11'),
(4, 'TEST TRELLO ', 'test maa ayoub', 'PLANNED', '2026-03-27', '2026-03-28', 10.00, 13, '2026-03-01 20:56:14'),
(5, 'trellooo', 'yo', 'PLANNED', '2026-03-24', '2026-03-31', 985.00, 13, '2026-03-01 21:30:48'),
(6, 'gta', 'd', 'IN_PROGRESS', '2026-03-09', '2026-03-25', 5.00, 13, '2026-03-01 21:44:46'),
(7, 'dazk', 'dezbfe', 'PLANNED', '2026-04-01', '2026-04-09', 55.00, 13, '2026-03-02 04:57:43');

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
CREATE TABLE IF NOT EXISTS `question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quiz_id` int NOT NULL,
  `enonce` text NOT NULL,
  `points` int NOT NULL DEFAULT '1',
  `ordre` int NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_published` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_question_quiz` (`quiz_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `question`
--

INSERT INTO `question` (`id`, `quiz_id`, `enonce`, `points`, `ordre`, `created_at`, `is_published`) VALUES
(1, 1, 'hellow', 1, 1, '2026-03-02 06:26:00', 1),
(2, 5, 'Quelle est la différence entre une variable et une constante dans le langage JavaScript?', 1, 1, '2026-03-02 08:07:39', 1),
(3, 5, 'Quelles sont les différentes types de variables disponibles dans JavaScript ?', 1, 2, '2026-03-02 08:07:39', 1),
(4, 5, 'Quel type de variable est utilisé pour stocker des chaînes de caractères dans JavaScript ?', 1, 3, '2026-03-02 08:07:39', 1),
(5, 5, 'Quelle est la différence entre une fonction et une méthode dans le contexte de JavaScript ?', 1, 4, '2026-03-02 08:07:39', 1),
(6, 5, 'Quels sont les différents types de variables en JavaScript ?', 1, 5, '2026-03-02 08:07:39', 1);

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
CREATE TABLE IF NOT EXISTS `quiz` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) NOT NULL,
  `description` text,
  `duree_minutes` int NOT NULL DEFAULT '10',
  `actif` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `seance_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_quiz_seance` (`seance_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`id`, `titre`, `description`, `duree_minutes`, `actif`, `created_at`, `seance_id`) VALUES
(1, 'jfioezjoif', 'fehuihuer', 30, 1, '2026-03-02 07:25:28', 1),
(2, 'jazz', '4d56d', 30, 1, '2026-03-02 08:02:52', 2),
(3, 'Javafx', 'javafx', 30, 1, '2026-03-02 08:03:31', 3),
(4, 'java script Basics', 'basic javascript', 30, 1, '2026-03-02 08:33:10', 4),
(5, 'Javascript Basics', 'variabales and types', 30, 1, '2026-03-02 08:51:03', 5);

-- --------------------------------------------------------

--
-- Table structure for table `seance`
--

DROP TABLE IF EXISTS `seance`;
CREATE TABLE IF NOT EXISTS `seance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `formation_id` int NOT NULL,
  `titre` varchar(150) NOT NULL,
  `type` enum('PRESENTIEL','EN_LIGNE') NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL,
  `duree_minutes` int DEFAULT NULL,
  `statut` enum('PLANIFIEE','EN_COURS','TERMINEE') DEFAULT 'PLANIFIEE',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `formation_id` (`formation_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `seance`
--

INSERT INTO `seance` (`id`, `formation_id`, `titre`, `type`, `date_debut`, `date_fin`, `adresse`, `latitude`, `longitude`, `video_path`, `duree_minutes`, `statut`, `created_at`) VALUES
(1, 1, 'gta', 'EN_LIGNE', '2026-03-02 08:00:00', '2026-03-02 10:00:00', NULL, NULL, NULL, 'hhhhh', NULL, 'PLANIFIEE', '2026-03-02 06:25:19'),
(3, 1, 'javafx', 'EN_LIGNE', '2026-03-02 08:00:00', '2026-03-02 10:00:00', NULL, NULL, NULL, 'https://www.youtube.com/watch?v=H6mfWun73vI&list=RDH6mfWun73vI&start_radio=1', NULL, 'PLANIFIEE', '2026-03-02 07:03:13'),
(4, 1, 'javascript', 'EN_LIGNE', '2026-03-02 08:00:00', '2026-03-02 10:00:00', NULL, NULL, NULL, 'https://youtu.be/IVJs_eSruLg?si=6C_kyhXnCfeY3zQR', NULL, 'PLANIFIEE', '2026-03-02 07:32:44'),
(5, 1, 'gta', 'PRESENTIEL', '2026-03-02 08:00:00', '2026-03-02 10:00:00', 'بو حناش, معتمدية قلعة الأندلس, ولاية أريانة, تونس', 36.96972960762463, 10.12364387512207, NULL, NULL, 'PLANIFIEE', '2026-03-02 07:42:58');

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
-- Table structure for table `tentative_quiz`
--

DROP TABLE IF EXISTS `tentative_quiz`;
CREATE TABLE IF NOT EXISTS `tentative_quiz` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quiz_id` int NOT NULL,
  `user_id` int NOT NULL,
  `candidat_nom` varchar(255) DEFAULT NULL,
  `candidat_email` varchar(255) DEFAULT NULL,
  `score` int NOT NULL DEFAULT '0',
  `total` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_tentative_quiz` (`quiz_id`),
  KEY `fk_tentative_quiz_user` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `tentative_quiz`
--

INSERT INTO `tentative_quiz` (`id`, `quiz_id`, `user_id`, `candidat_nom`, `candidat_email`, `score`, `total`, `created_at`) VALUES
(1, 1, 19, NULL, NULL, 1, 1, '2026-03-02 07:29:01'),
(2, 5, 19, NULL, NULL, 3, 5, '2026-03-02 09:13:52');

-- --------------------------------------------------------

--
-- Table structure for table `tentative_seance`
--

DROP TABLE IF EXISTS `tentative_seance`;
CREATE TABLE IF NOT EXISTS `tentative_seance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `seance_id` int NOT NULL,
  `quiz_id` int NOT NULL,
  `user_id` int NOT NULL,
  `candidat_email` varchar(255) DEFAULT NULL,
  `score` int NOT NULL DEFAULT '0',
  `total` int NOT NULL DEFAULT '0',
  `statut` enum('EN_COURS','SOUMISE','VALIDEE','ECHOUEE') DEFAULT 'EN_COURS',
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `ended_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_candidat_seance` (`seance_id`,`user_id`),
  KEY `quiz_id` (`quiz_id`),
  KEY `fk_tentative_seance_user` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
