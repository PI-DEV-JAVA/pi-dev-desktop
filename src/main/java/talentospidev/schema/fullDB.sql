-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: db:3306
-- Generation Time: Feb 28, 2026 at 08:27 PM
-- Server version: 8.1.0
-- PHP Version: 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `main`
--

-- --------------------------------------------------------

--
-- Table structure for table `activities`
--

CREATE TABLE `activities` (
  `id_activity` int NOT NULL,
  `employee_id` int NOT NULL,
  `activity_date` date NOT NULL,
  `description` text,
  `hours_worked` decimal(5,2) DEFAULT NULL,
  `project_id` int DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `activities`
--

INSERT INTO `activities` (`id_activity`, `employee_id`, `activity_date`, `description`, `hours_worked`, `project_id`) VALUES
(4, 18, '2026-02-18', 'test', 100.00, 1);

-- --------------------------------------------------------

--
-- Table structure for table `activity_files`
--

CREATE TABLE `activity_files` (
  `id` int NOT NULL,
  `activity_id` int NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `applications`
--

CREATE TABLE `applications` (
  `id` int NOT NULL,
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
  `response_date` date DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `applications`
--

INSERT INTO `applications` (`id`, `user_id`, `offer_id`, `cv_file_path`, `motivation_letter`, `status`, `application_date`, `score`, `notes`, `interviewer`, `interview_date`, `interview_result`, `recruiter_response`, `response_date`) VALUES
(1, 14, 2, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hey there', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 15, 2, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hey', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 14, 3, 'C:\\Users\\ayoub\\Downloads\\RH (3).pdf', 'hello', 'Acceptée', '2026-02-26', 0, NULL, NULL, NULL, NULL, 'très bien', '2026-02-26'),
(4, 17, 3, NULL, '', 'Refusée', '2026-02-26', 0, NULL, NULL, NULL, NULL, 'your profile is incomplete', '2026-02-26'),
(5, 17, 2, NULL, '', 'Nouvelle', '2026-02-26', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 18, 3, '/home/baril/Downloads/Application RH de Recrutement Intelligent.pdf', 'please accept me', 'Nouvelle', '2026-02-28', 0, NULL, NULL, NULL, NULL, NULL, NULL),
(7, 18, 4, '/home/baril/ospf4.pdf', 'yo please accept me', 'Acceptée', '2026-02-28', 0, NULL, NULL, NULL, NULL, 'ok you got it', '2026-02-28');

-- --------------------------------------------------------

--
-- Table structure for table `offers`
--

CREATE TABLE `offers` (
  `id` int NOT NULL,
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
  `recruiter_id` int NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `offers`
--

INSERT INTO `offers` (`id`, `title`, `description`, `department`, `contract_type`, `experience_level`, `salary_min`, `salary_max`, `location`, `status`, `publish_date`, `closing_date`, `positions_available`, `applications_received`, `recruiter_id`) VALUES
(2, 'ijio', 'fejzifjjifezjoi', 'IT', 'CDI', 'Senior', 4555, 11555, 'Tunis', 'Ouverte', '2026-02-23', '2026-03-25', 6, 4, 0),
(3, 'dev', 'hey there', 'Finance', 'CDI', 'Junior', 1000, 1666, 'tunis', 'Ouverte', '2026-02-26', '2026-03-27', 12, 3, 13),
(4, 'validation', 'this is a validation test', 'IT', 'CDD', 'Junior', 1000, 2000, 'tunis', 'Ouverte', '2026-02-28', '2026-03-28', 1, 1, 19);

-- --------------------------------------------------------

--
-- Table structure for table `profiles`
--

CREATE TABLE `profiles` (
  `id` int NOT NULL,
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
  `cv_path` varchar(500) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
(14, 17, 'omar', 'hamdi', '2004-02-20', '99666888', 'ariana ville', 'Student', 2, 'I love Nafti.', 1, 'C:\\Users\\ayoub\\Desktop\\fullmark.jpg', NULL),
(15, 18, 'Hamdi', 'Omar', '2004-10-27', '20404237', 'Tunisia', 'Etudiant', 0, 'yo this is me trying to debug', 1, '18.png', NULL),
(16, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `project`
--

CREATE TABLE `project` (
  `id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  `status` enum('PLANNED','IN_PROGRESS','DONE','ON_HOLD') DEFAULT 'PLANNED',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `budget` decimal(10,2) DEFAULT NULL,
  `project_manager_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`id`, `name`, `description`, `status`, `start_date`, `end_date`, `budget`, `project_manager_id`, `created_at`) VALUES
(1, 'Validation', 'yo this is a test ', 'PLANNED', '2026-02-05', '2026-03-06', 100.00, 19, '2026-02-28 20:00:19');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `email` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','HR','CANDIDATE') NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `auth_provider` enum('LOCAL','GOOGLE') NOT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `email_verified` tinyint(1) DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `role`, `active`, `created_at`, `auth_provider`, `provider_id`, `email_verified`) VALUES
(1, 'ayoubhamed@esprit.tn', '62d18522b74d75b2a84776c91ba5498377441d4c4af0cea22ca7de9e09475d3a', 'CANDIDATE', 1, '2026-02-09 01:34:01', 'LOCAL', NULL, 0),
(2, 'ayoubhamed111@gmail.com', '6af78956120b7b454897fc521aa07928e8fd0de00b10eb67de30f4ce10cbc258', 'CANDIDATE', 0, '2026-02-09 02:58:01', 'LOCAL', NULL, 0),
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
(17, 'omar@hamdi.tn', '87e5c999eb63fa472d4498109348861923598f1fe7cb36382def69071bb9df5a', 'CANDIDATE', 1, '2026-02-26 22:42:52', 'LOCAL', NULL, 0),
(18, 'omar.hamdi204@gmail.com', 'c86325f8a0c9922dfe5e73d89be7eeda2334d70b48e2cb7370cddc497790c259', 'CANDIDATE', 1, '2026-02-28 19:54:52', 'LOCAL', NULL, 0),
(19, 'Hamdii.Omar204@gmail.com', 'c86325f8a0c9922dfe5e73d89be7eeda2334d70b48e2cb7370cddc497790c259', 'HR', 1, '2026-02-28 19:58:09', 'LOCAL', NULL, 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activities`
--
ALTER TABLE `activities`
  ADD PRIMARY KEY (`id_activity`),
  ADD KEY `fk_activity_employee` (`employee_id`),
  ADD KEY `fk_activity_project` (`project_id`);

--
-- Indexes for table `applications`
--
ALTER TABLE `applications`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_offer` (`user_id`,`offer_id`),
  ADD KEY `offer_id` (`offer_id`);

--
-- Indexes for table `offers`
--
ALTER TABLE `offers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `f1` (`recruiter_id`);

--
-- Indexes for table `profiles`
--
ALTER TABLE `profiles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- Indexes for table `project`
--
ALTER TABLE `project`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activities`
--
ALTER TABLE `activities`
  MODIFY `id_activity` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `applications`
--
ALTER TABLE `applications`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `offers`
--
ALTER TABLE `offers`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `profiles`
--
ALTER TABLE `profiles`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `project`
--
ALTER TABLE `project`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;