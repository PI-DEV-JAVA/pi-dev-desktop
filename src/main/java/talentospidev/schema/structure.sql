-- MySQL dump 10.13  Distrib 8.1.0, for Linux (x86_64)
--
-- Host: localhost    Database: main
-- ------------------------------------------------------
-- Server version	8.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `interview`
--

DROP TABLE IF EXISTS `interview`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview` (
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
  KEY `idx_interview_status` (`status`),
  CONSTRAINT `fk_interview_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_interview_recruiter` FOREIGN KEY (`recruiter_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interview_meet`
--

DROP TABLE IF EXISTS `interview_meet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_meet` (
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
  KEY `idx_meet_schedule` (`scheduled_at`),
  CONSTRAINT `fk_meet_interview` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interview_note`
--

DROP TABLE IF EXISTS `interview_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `interview_id` bigint NOT NULL,
  `recruiter_id` bigint NOT NULL,
  `note` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_note_interview` (`interview_id`),
  KEY `fk_note_recruiter` (`recruiter_id`),
  CONSTRAINT `fk_note_interview` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_note_recruiter` FOREIGN KEY (`recruiter_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `role` enum('RECRUITER','CANDIDATE') NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-24 20:52:35
