CREATE TABLE IF NOT EXISTS `bookmarks` (
  `id`           INT NOT NULL AUTO_INCREMENT,
  `candidate_id` INT NOT NULL,
  `offer_id`     INT NOT NULL,
  `saved_at`     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `notes`        TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_bookmark`        (`candidate_id`, `offer_id`),
  KEY `idx_bookmarks_candidate` (`candidate_id`),
  KEY `idx_bookmarks_offer`     (`offer_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;