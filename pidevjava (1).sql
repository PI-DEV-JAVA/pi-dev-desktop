-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 02, 2026 at 05:53 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pidevjava`
--

-- --------------------------------------------------------

--
-- Table structure for table `choix`
--

CREATE TABLE `choix` (
  `id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `texte` text NOT NULL,
  `est_correct` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `choix`
--

INSERT INTO `choix` (`id`, `question_id`, `texte`, `est_correct`, `created_at`) VALUES
(55, 19, 'Programmation Orientée Objet', 1, '2026-03-01 14:55:23'),
(56, 19, 'Programme Objet Organisé', 0, '2026-03-01 14:55:23'),
(57, 19, 'Processus Orienté Objet', 0, '2026-03-01 14:55:23'),
(58, 19, 'Programming Online Object', 0, '2026-03-01 14:55:23'),
(59, 20, 'extends', 1, '2026-03-01 14:55:23'),
(60, 20, 'implements', 0, '2026-03-01 14:55:23'),
(61, 20, 'inherit', 0, '2026-03-01 14:55:23'),
(62, 20, 'super', 0, '2026-03-01 14:55:23'),
(67, 22, 'Décorer une interface', 0, '2026-03-01 22:03:17'),
(68, 22, 'Comprendre les concepts clés', 1, '2026-03-01 22:03:17'),
(69, 22, 'Éviter les tests', 0, '2026-03-01 22:03:17'),
(70, 22, 'Supprimer la base de données', 0, '2026-03-01 22:03:17'),
(71, 23, 'Ignorer la validation', 0, '2026-03-01 22:03:17'),
(72, 23, 'Tester et valider', 1, '2026-03-01 22:03:17'),
(73, 23, 'Coder sans structure', 0, '2026-03-01 22:03:17'),
(74, 23, 'Ne jamais documenter', 0, '2026-03-01 22:03:17'),
(75, 24, 'Chance', 0, '2026-03-01 22:03:17'),
(76, 24, 'Compréhension', 1, '2026-03-01 22:03:17'),
(77, 24, 'Vitesse uniquement', 0, '2026-03-01 22:03:17'),
(78, 24, 'Copier-coller', 0, '2026-03-01 22:03:17'),
(79, 25, 'Ambiguë', 0, '2026-03-01 22:03:17'),
(80, 25, 'Claire et précise', 1, '2026-03-01 22:03:17'),
(81, 25, 'Très longue', 0, '2026-03-01 22:03:17'),
(82, 25, 'Sans réponses', 0, '2026-03-01 22:03:17'),
(83, 26, 'Pour piéger sans logique', 0, '2026-03-01 22:03:17'),
(84, 26, 'Pour évaluer la compréhension', 1, '2026-03-01 22:03:17'),
(85, 26, 'Pour rendre le quiz inutile', 0, '2026-03-01 22:03:17'),
(86, 26, 'Pour bloquer le candidat', 0, '2026-03-01 22:03:17'),
(87, 27, 'Pratiquer et réviser', 1, '2026-03-01 22:03:17'),
(88, 27, 'Éviter la pratique', 0, '2026-03-01 22:03:17'),
(89, 27, 'Ignorer les exercices', 0, '2026-03-01 22:03:17'),
(90, 27, 'Ne jamais poser de questions', 0, '2026-03-01 22:03:17'),
(91, 28, 'Punir', 0, '2026-03-01 22:03:17'),
(92, 28, 'Améliorer l’apprentissage', 1, '2026-03-01 22:03:17'),
(93, 28, 'Supprimer le score', 0, '2026-03-01 22:03:17'),
(94, 28, 'Éviter la progression', 0, '2026-03-01 22:03:17'),
(95, 29, 'Perdre du temps', 0, '2026-03-01 22:03:17'),
(96, 29, 'Structurer l’apprentissage', 1, '2026-03-01 22:03:17'),
(97, 29, 'Éviter le contenu', 0, '2026-03-01 22:03:17'),
(98, 29, 'Supprimer les objectifs', 0, '2026-03-01 22:03:17'),
(99, 30, 'Décorer une interface', 0, '2026-03-01 22:03:58'),
(100, 30, 'Comprendre les concepts clés', 1, '2026-03-01 22:03:58'),
(101, 30, 'Éviter les tests', 0, '2026-03-01 22:03:58'),
(102, 30, 'Supprimer la base de données', 0, '2026-03-01 22:03:58'),
(103, 31, 'Ignorer la validation', 0, '2026-03-01 22:03:58'),
(104, 31, 'Tester et valider', 1, '2026-03-01 22:03:58'),
(105, 31, 'Coder sans structure', 0, '2026-03-01 22:03:58'),
(106, 31, 'Ne jamais documenter', 0, '2026-03-01 22:03:58'),
(107, 32, 'Chance', 0, '2026-03-01 22:03:58'),
(108, 32, 'Compréhension', 1, '2026-03-01 22:03:58'),
(109, 32, 'Vitesse uniquement', 0, '2026-03-01 22:03:58'),
(110, 32, 'Copier-coller', 0, '2026-03-01 22:03:58'),
(111, 33, 'Ambiguë', 0, '2026-03-01 22:03:58'),
(112, 33, 'Claire et précise', 1, '2026-03-01 22:03:58'),
(113, 33, 'Très longue', 0, '2026-03-01 22:03:58'),
(114, 33, 'Sans réponses', 0, '2026-03-01 22:03:58'),
(115, 34, 'Pour piéger sans logique', 0, '2026-03-01 22:03:58'),
(116, 34, 'Pour évaluer la compréhension', 1, '2026-03-01 22:03:58'),
(117, 34, 'Pour rendre le quiz inutile', 0, '2026-03-01 22:03:58'),
(118, 34, 'Pour bloquer le candidat', 0, '2026-03-01 22:03:58'),
(119, 35, 'Pratiquer et réviser', 1, '2026-03-01 22:03:58'),
(120, 35, 'Éviter la pratique', 0, '2026-03-01 22:03:58'),
(121, 35, 'Ignorer les exercices', 0, '2026-03-01 22:03:58'),
(122, 35, 'Ne jamais poser de questions', 0, '2026-03-01 22:03:58'),
(123, 36, 'Punir', 0, '2026-03-01 22:03:58'),
(124, 36, 'Améliorer l’apprentissage', 1, '2026-03-01 22:03:58'),
(125, 36, 'Supprimer le score', 0, '2026-03-01 22:03:58'),
(126, 36, 'Éviter la progression', 0, '2026-03-01 22:03:58'),
(127, 37, 'Perdre du temps', 0, '2026-03-01 22:03:58'),
(128, 37, 'Structurer l’apprentissage', 1, '2026-03-01 22:03:58'),
(129, 37, 'Éviter le contenu', 0, '2026-03-01 22:03:58'),
(130, 37, 'Supprimer les objectifs', 0, '2026-03-01 22:03:58'),
(131, 38, 'Créer une scène', 1, '2026-03-02 00:55:15'),
(132, 38, 'Définir les styles CSS', 0, '2026-03-02 00:55:15'),
(133, 38, 'Ajouter des widgets', 0, '2026-03-02 00:55:15'),
(134, 38, 'Configurer le layout', 0, '2026-03-02 00:55:15'),
(135, 39, 'Cliquez sur le bouton \"Add\" et sélectionnez le widget', 1, '2026-03-02 00:55:15'),
(136, 39, 'Copiez le code du widget puis collez-le dans le cadre', 0, '2026-03-02 00:55:15'),
(137, 39, 'Choisissez le type de widget dans le menu déroulant', 0, '2026-03-02 00:55:15'),
(138, 39, 'Incluez le widget dans le cadre via le menu contextuel', 0, '2026-03-02 00:55:15'),
(139, 40, 'Construire le modèle graphique', 1, '2026-03-02 00:55:15'),
(140, 40, 'Écrire le code Java', 0, '2026-03-02 00:55:15'),
(141, 40, 'Gérer les animations', 0, '2026-03-02 00:55:15'),
(142, 40, 'Optimiser le rendu', 0, '2026-03-02 00:55:15'),
(143, 41, 'JavaFX SceneBuilder', 1, '2026-03-02 00:55:15'),
(144, 41, 'Photoshop', 0, '2026-03-02 00:55:15'),
(145, 41, 'Illustrator', 0, '2026-03-02 00:55:15'),
(146, 41, 'Adobe XD', 0, '2026-03-02 00:55:15'),
(147, 42, 'Pour créer des scènes simples', 1, '2026-03-02 00:55:15'),
(148, 42, 'Pour coder des applications JavaFX', 0, '2026-03-02 00:55:15'),
(149, 42, 'Pour dessiner des images', 0, '2026-03-02 00:55:15'),
(150, 42, 'Pour générer des interfaces utilisateur', 0, '2026-03-02 00:55:15'),
(151, 43, 'sudo apt-get install avrdude', 0, '2026-03-02 02:26:31'),
(152, 43, 'sudo apt-get install python3-pip', 1, '2026-03-02 02:26:31'),
(153, 43, 'sudo apt-get install git', 0, '2026-03-02 02:26:31'),
(154, 43, 'sudo apt-get install gcc-arm-none-eabi', 0, '2026-03-02 02:26:31'),
(155, 44, 'C++', 0, '2026-03-02 02:26:31'),
(156, 44, 'Python', 0, '2026-03-02 02:26:31'),
(157, 44, 'Java', 0, '2026-03-02 02:26:31'),
(158, 44, 'Arduino', 1, '2026-03-02 02:26:31'),
(159, 45, 'Comprendre et manipuler les données', 1, '2026-03-02 02:26:31'),
(160, 45, 'Programmer des logiciels', 0, '2026-03-02 02:26:31'),
(161, 45, 'Connaître les composants électroniques', 0, '2026-03-02 02:26:31'),
(162, 45, 'Sauvegarder les programmes', 0, '2026-03-02 02:26:31'),
(163, 46, 'Une carte microcontrôleur est moins puissante', 0, '2026-03-02 02:26:31'),
(164, 46, 'Une carte microcontrôleur n\'a pas de RAM', 0, '2026-03-02 02:26:31'),
(165, 46, 'Une carte microcontrôleur n\'est pas programmable', 1, '2026-03-02 02:26:31'),
(166, 46, 'Une carte microcontrôleur n\'a pas de connecteurs USB', 0, '2026-03-02 02:26:31'),
(167, 47, 'Comprendre et manipuler les données', 1, '2026-03-02 02:26:31'),
(168, 47, 'Programmer des logiciels', 0, '2026-03-02 02:26:31'),
(169, 47, 'Connaître les composants électroniques', 0, '2026-03-02 02:26:31'),
(170, 47, 'Sauvegarder les programmes', 0, '2026-03-02 02:26:31'),
(171, 48, 'Une carte Arduino est un ordinateur', 1, '2026-03-02 03:47:43'),
(172, 48, 'Un ordinateur est un ensemble de cartes Arduino', 0, '2026-03-02 03:47:43'),
(173, 48, 'Les cartes Arduino sont moins puissantes que les ordinateurs', 0, '2026-03-02 03:47:43'),
(174, 48, 'Les cartes Arduino ne peuvent pas être utilisées pour exécuter du code', 0, '2026-03-02 03:47:43'),
(175, 49, 'Lecture de données physiques', 1, '2026-03-02 03:47:43'),
(176, 49, 'Exécution de programmes complexes', 0, '2026-03-02 03:47:43'),
(177, 49, 'Transmission de données', 0, '2026-03-02 03:47:43'),
(178, 49, 'Détection de signaux sonores', 0, '2026-03-02 03:47:44'),
(179, 50, 'Connecteurs USB', 0, '2026-03-02 03:47:44'),
(180, 50, 'Connecteurs GPIO', 1, '2026-03-02 03:47:44'),
(181, 50, 'Connecteurs HDMI', 0, '2026-03-02 03:47:44'),
(182, 50, 'Connecteurs Ethernet', 0, '2026-03-02 03:47:44'),
(183, 51, 'digitalRead()', 1, '2026-03-02 03:47:44'),
(184, 51, 'analogRead()', 0, '2026-03-02 03:47:44'),
(185, 51, 'Serial.print()', 0, '2026-03-02 03:47:44'),
(186, 51, 'delay()', 0, '2026-03-02 03:47:44'),
(187, 52, 'tone()', 1, '2026-03-02 03:47:44'),
(188, 52, 'playSound()', 0, '2026-03-02 03:47:44'),
(189, 52, 'soundOn()', 0, '2026-03-02 03:47:44'),
(190, 52, 'playNote()', 0, '2026-03-02 03:47:44');

-- --------------------------------------------------------

--
-- Table structure for table `formation`
--

CREATE TABLE `formation` (
  `id` int(11) NOT NULL,
  `nom` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `contenu` longtext DEFAULT NULL,
  `difficulte` enum('DEBUTANT','INTERMEDIAIRE','AVANCE') NOT NULL,
  `categorie` varchar(80) NOT NULL,
  `mode` enum('EN_LIGNE','PRESENTIEL','HYBRIDE') DEFAULT 'EN_LIGNE',
  `lieu` varchar(120) DEFAULT NULL,
  `formateur` varchar(120) DEFAULT NULL,
  `prerequis` text DEFAULT NULL,
  `capacite_max` int(11) DEFAULT 0,
  `statut` enum('OUVERTE','EN_COURS','TERMINEE') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `formation`
--

INSERT INTO `formation` (`id`, `nom`, `description`, `date_debut`, `date_fin`, `contenu`, `difficulte`, `categorie`, `mode`, `lieu`, `formateur`, `prerequis`, `capacite_max`, `statut`, `created_at`) VALUES
(25, 'Full Java Backend', 'Formation complète Java + SQL + Quiz', '2026-03-05', '2026-04-05', 'POO + JDBC + Quiz System', 'INTERMEDIAIRE', 'Développement', 'PRESENTIEL', 'ESPRIT', 'Mr. Ayoub', 'Java Basics', 25, 'OUVERTE', '2026-03-01 14:55:22'),
(28, 'zzz', '', '2026-02-27', '2026-03-27', '', 'DEBUTANT', 'Développement', 'PRESENTIEL', '', '', '', 4, 'OUVERTE', '2026-03-01 19:46:01'),
(30, 'Arduino', 'formation de a jusqua z en arduino', '2026-03-02', '2026-04-02', '4 seances presenielles', 'INTERMEDIAIRE', 'Développement', 'PRESENTIEL', 'tekup', 'dali', 'pc + souris + intelligence', 30, 'OUVERTE', '2026-03-02 02:17:32');

-- --------------------------------------------------------

--
-- Table structure for table `inscription`
--

CREATE TABLE `inscription` (
  `id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL,
  `candidat_nom` varchar(120) NOT NULL,
  `candidat_email` varchar(120) NOT NULL,
  `date_inscription` datetime DEFAULT current_timestamp(),
  `statut` varchar(30) DEFAULT 'EN_ATTENTE',
  `score_quiz` double DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inscription`
--

INSERT INTO `inscription` (`id`, `formation_id`, `candidat_nom`, `candidat_email`, `date_inscription`, `statut`, `score_quiz`, `created_at`) VALUES
(15, 25, 'Yassine Chaabene', 'yassinechaabene@gmail.com', '2026-03-01 16:06:07', 'ACCEPTEE', 20, '2026-03-01 16:06:07'),
(19, 30, 'Yassine Chaabene', 'yassinechaabene@gmail.com', '2026-03-02 03:20:18', 'ACCEPTEE', 4, '2026-03-02 03:20:18');

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `enonce` text NOT NULL,
  `points` int(11) NOT NULL DEFAULT 1,
  `ordre` int(11) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_published` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `question`
--

INSERT INTO `question` (`id`, `quiz_id`, `enonce`, `points`, `ordre`, `created_at`, `is_published`) VALUES
(19, 23, 'Que signifie POO ?', 1, 1, '2026-03-01 14:55:23', 1),
(20, 23, 'Quel mot-clé permet l’héritage ?', 1, 2, '2026-03-01 14:55:23', 1),
(22, 25, 'Quel est l’objectif principal de \"Quiz DAO\" ?', 1, 1, '2026-03-01 22:03:17', 1),
(23, 25, 'Dans le contexte : Quiz sur DAO, quelle est la meilleure pratique ?', 1, 1, '2026-03-01 22:03:17', 1),
(24, 25, 'Quel élément est le plus important pour réussir un quiz ?', 1, 1, '2026-03-01 22:03:17', 1),
(25, 25, 'Une question QCM doit être :', 1, 1, '2026-03-01 22:03:17', 1),
(26, 25, 'Pourquoi utiliser des choix plausibles ?', 1, 1, '2026-03-01 22:03:17', 1),
(27, 25, 'La meilleure façon d’apprendre \"Quiz DAO\" est :', 1, 1, '2026-03-01 22:03:17', 1),
(28, 25, 'Un bon feedback après un quiz sert à :', 1, 1, '2026-03-01 22:03:17', 1),
(29, 25, 'Quel est l’intérêt d’une séance (présentiel/en ligne) ?', 1, 1, '2026-03-01 22:03:17', 1),
(30, 30, 'Quel est l’objectif principal de \"quiz chotrana\" ?', 1, 1, '2026-03-01 22:03:58', 1),
(31, 30, 'Dans le contexte : chotrana, quelle est la meilleure pratique ?', 1, 1, '2026-03-01 22:03:58', 1),
(32, 30, 'Quel élément est le plus important pour réussir un quiz ?', 1, 1, '2026-03-01 22:03:58', 1),
(33, 30, 'Une question QCM doit être :', 1, 1, '2026-03-01 22:03:58', 1),
(34, 30, 'Pourquoi utiliser des choix plausibles ?', 1, 1, '2026-03-01 22:03:58', 1),
(35, 30, 'La meilleure façon d’apprendre \"quiz chotrana\" est :', 1, 1, '2026-03-01 22:03:58', 1),
(36, 30, 'Un bon feedback après un quiz sert à :', 1, 1, '2026-03-01 22:03:58', 1),
(37, 30, 'Quel est l’intérêt d’une séance (présentiel/en ligne) ?', 1, 1, '2026-03-01 22:03:58', 1),
(38, 33, 'Quelle est la fonction principale d\'un composant SceneBuilder dans JavaFX?', 1, 1, '2026-03-02 00:55:15', 1),
(39, 33, 'Comment ajouter un widget personnalisé dans SceneBuilder ?', 1, 1, '2026-03-02 00:55:15', 1),
(40, 33, 'Quel est le rôle principal d\'un designer de scènes dans JavaFX ?', 1, 1, '2026-03-02 00:55:15', 1),
(41, 33, 'Quelles sont les principales ressources utilisées pour créer des scènes avec SceneBuilder ?', 1, 1, '2026-03-02 00:55:15', 1),
(42, 33, 'Quand utiliser SceneBuilder ?', 1, 1, '2026-03-02 00:55:15', 1),
(43, 34, 'Quelle est la première commande que vous devrez entrer dans votre ordinateur pour utiliser Arduino ?', 1, 1, '2026-03-02 02:26:31', 1),
(44, 34, 'Quel est le nom du premier langage d\'programmation utilisé par Arduino ?', 1, 1, '2026-03-02 02:26:31', 1),
(45, 34, 'Quelle est la fonction principale d\'Arduino ?', 1, 1, '2026-03-02 02:26:31', 1),
(46, 34, 'Quelle est la différence entre une carte microcontrôleur et une carte Arduino ?', 1, 1, '2026-03-02 02:26:31', 1),
(47, 34, 'Quelle est la fonction principale d\'une carte microcontrôleur ?', 1, 1, '2026-03-02 02:26:31', 1),
(48, 35, 'Quelle est la principale différence entre une carte Arduino et un ordinateur?', 1, 1, '2026-03-02 03:47:43', 1),
(49, 35, 'Quelles fonctions de base de codage permettent d\"interagir avec le monde physique via Arduino?', 1, 1, '2026-03-02 03:47:43', 1),
(50, 35, 'Quel type de connecteur est utilisé pour accéder aux pins internes de la carte Arduino?', 1, 1, '2026-03-02 03:47:44', 1),
(51, 35, 'Quelle commande Arduino utilise-t-on pour lire une valeur numérique depuis un capteur sensorique?', 1, 1, '2026-03-02 03:47:44', 1),
(52, 35, 'Quelle commande Arduino utilise-t-on pour émettre une note sonore?', 1, 1, '2026-03-02 03:47:44', 1);

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

CREATE TABLE `quiz` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `duree_minutes` int(11) NOT NULL DEFAULT 10,
  `actif` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `seance_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`id`, `titre`, `description`, `duree_minutes`, `actif`, `created_at`, `seance_id`) VALUES
(23, 'Quiz POO', 'Quiz sur POO', 15, 1, '2026-03-01 15:55:23', 7),
(24, 'Quiz JDBC', 'Quiz sur JDBC', 15, 1, '2026-03-01 15:55:23', 8),
(25, 'Quiz DAO', 'Quiz sur DAO', 15, 1, '2026-03-01 15:55:23', 9),
(30, 'quiz chotrana', 'chotrana', 30, 1, '2026-03-01 18:51:22', NULL),
(31, 'adada', 'dadd', 30, 1, '2026-03-01 20:22:12', NULL),
(32, 'quiz dd', 'abd', 30, 1, '2026-03-01 20:46:52', 14),
(33, 'quiz seance 6 javaFX', 'scenebuilder etc', 30, 1, '2026-03-01 23:47:03', 16),
(34, 'quiz introduction arduino', 'connaitre tous les bases sur arduino', 40, 1, '2026-03-02 03:19:48', 17),
(35, 'atelier pratique arduino', 'connaitre les hardware et quelques fonctionalités de coding de base sur arduino', 30, 1, '2026-03-02 04:30:52', 18);

-- --------------------------------------------------------

--
-- Table structure for table `seance`
--

CREATE TABLE `seance` (
  `id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL,
  `titre` varchar(150) NOT NULL,
  `type` enum('PRESENTIEL','EN_LIGNE') NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL,
  `duree_minutes` int(11) DEFAULT NULL,
  `statut` enum('PLANIFIEE','EN_COURS','TERMINEE') DEFAULT 'PLANIFIEE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `seance`
--

INSERT INTO `seance` (`id`, `formation_id`, `titre`, `type`, `date_debut`, `date_fin`, `adresse`, `latitude`, `longitude`, `video_path`, `duree_minutes`, `statut`, `created_at`) VALUES
(7, 25, 'Séance 1 - POO', 'PRESENTIEL', '2026-03-06 09:00:00', '2026-03-06 12:00:00', 'ESPRIT', 36.8986, 10.1897, NULL, 180, 'PLANIFIEE', '2026-03-01 14:55:23'),
(8, 25, 'Séance 2 - JDBC', 'EN_LIGNE', '2026-03-13 18:00:00', '2026-03-13 20:00:00', NULL, NULL, NULL, 'https://drive.google.com/test', 120, 'PLANIFIEE', '2026-03-01 14:55:23'),
(9, 25, 'Séance 3 - DAO', 'PRESENTIEL', '2026-03-20 09:00:00', '2026-03-20 12:00:00', 'ESPRIT', 36.8986, 10.1897, NULL, 180, 'PLANIFIEE', '2026-03-01 14:55:23'),
(10, 25, 'Séance 4 - Projet Final', 'EN_LIGNE', '2026-04-01 18:00:00', '2026-04-01 20:00:00', NULL, NULL, NULL, 'https://drive.google.com/test2', 120, 'PLANIFIEE', '2026-03-01 14:55:23'),
(14, 28, 'dd', 'PRESENTIEL', '2026-03-01 09:00:00', '2026-03-01 11:00:00', 'ddd', 36.89801814554322, 10.17539978027344, NULL, NULL, 'PLANIFIEE', '2026-03-01 19:46:28'),
(15, 28, 'ddd', 'PRESENTIEL', '2026-03-01 09:00:00', '2026-03-01 11:00:00', 'tekup', 36.89952819448472, 10.18638610839844, NULL, NULL, 'PLANIFIEE', '2026-03-01 19:47:04'),
(16, 25, 'seance 6', 'PRESENTIEL', '2026-03-01 09:00:00', '2026-03-01 11:00:00', 'esprit', 36.891428491413045, 10.186214447021486, NULL, NULL, 'PLANIFIEE', '2026-03-01 22:45:57'),
(17, 30, 'introduction arduino', 'PRESENTIEL', '2026-03-01 09:00:00', '2026-03-01 11:00:00', 'tekup', 36.89156578167686, 10.18638610839844, NULL, NULL, 'PLANIFIEE', '2026-03-02 02:19:06'),
(18, 30, 'atelier pratique', 'PRESENTIEL', '2026-03-02 09:00:00', '2026-03-02 11:00:00', 'esprit', 36.90062639312916, 10.17213821411133, NULL, NULL, 'PLANIFIEE', '2026-03-02 03:30:02');

-- --------------------------------------------------------

--
-- Table structure for table `tentative_quiz`
--

CREATE TABLE `tentative_quiz` (
  `id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `candidat_nom` varchar(255) NOT NULL,
  `score` int(11) NOT NULL DEFAULT 0,
  `total` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `candidat_email` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tentative_quiz`
--

INSERT INTO `tentative_quiz` (`id`, `quiz_id`, `candidat_nom`, `score`, `total`, `created_at`, `candidat_email`) VALUES
(8, 23, 'Test Candidat', 1, 2, '2026-03-01 16:23:01', 'test@demo.com'),
(9, 23, 'Test Candidat', 2, 2, '2026-03-01 16:26:40', 'yassinechaabene@gmail.com'),
(10, 23, 'Test Candidat', 2, 2, '2026-03-01 17:20:42', 'test@demo.com'),
(11, 34, 'Test Candidat', 1, 5, '2026-03-02 03:29:51', 'yassinechaabene@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `tentative_seance`
--

CREATE TABLE `tentative_seance` (
  `id` int(11) NOT NULL,
  `seance_id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `candidat_email` varchar(255) NOT NULL,
  `score` int(11) NOT NULL DEFAULT 0,
  `total` int(11) NOT NULL DEFAULT 0,
  `statut` enum('EN_COURS','SOUMISE','VALIDEE','ECHOUEE') DEFAULT 'EN_COURS',
  `started_at` datetime DEFAULT current_timestamp(),
  `ended_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tentative_seance`
--

INSERT INTO `tentative_seance` (`id`, `seance_id`, `quiz_id`, `candidat_email`, `score`, `total`, `statut`, `started_at`, `ended_at`) VALUES
(1, 7, 23, 'yassinechaabene@gmail.com', 2, 2, 'SOUMISE', '2026-03-01 16:26:40', '2026-03-01 16:26:40'),
(2, 17, 34, 'yassinechaabene@gmail.com', 1, 5, 'SOUMISE', '2026-03-02 03:29:51', '2026-03-02 03:29:51');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `choix`
--
ALTER TABLE `choix`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_choix_question` (`question_id`);

--
-- Indexes for table `formation`
--
ALTER TABLE `formation`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `inscription`
--
ALTER TABLE `inscription`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_insc_formation` (`formation_id`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_question_quiz` (`quiz_id`);

--
-- Indexes for table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_quiz_seance` (`seance_id`);

--
-- Indexes for table `seance`
--
ALTER TABLE `seance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `formation_id` (`formation_id`);

--
-- Indexes for table `tentative_quiz`
--
ALTER TABLE `tentative_quiz`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_tentative_quiz` (`quiz_id`);

--
-- Indexes for table `tentative_seance`
--
ALTER TABLE `tentative_seance`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_candidat_seance` (`seance_id`,`candidat_email`),
  ADD KEY `quiz_id` (`quiz_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `choix`
--
ALTER TABLE `choix`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=191;

--
-- AUTO_INCREMENT for table `formation`
--
ALTER TABLE `formation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `inscription`
--
ALTER TABLE `inscription`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT for table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `seance`
--
ALTER TABLE `seance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `tentative_quiz`
--
ALTER TABLE `tentative_quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `tentative_seance`
--
ALTER TABLE `tentative_seance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `choix`
--
ALTER TABLE `choix`
  ADD CONSTRAINT `fk_choix_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `inscription`
--
ALTER TABLE `inscription`
  ADD CONSTRAINT `fk_insc_formation` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `quiz`
--
ALTER TABLE `quiz`
  ADD CONSTRAINT `fk_quiz_seance` FOREIGN KEY (`seance_id`) REFERENCES `seance` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `seance`
--
ALTER TABLE `seance`
  ADD CONSTRAINT `seance_ibfk_1` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `tentative_quiz`
--
ALTER TABLE `tentative_quiz`
  ADD CONSTRAINT `fk_tentative_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `tentative_seance`
--
ALTER TABLE `tentative_seance`
  ADD CONSTRAINT `tentative_seance_ibfk_1` FOREIGN KEY (`seance_id`) REFERENCES `seance` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `tentative_seance_ibfk_2` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
