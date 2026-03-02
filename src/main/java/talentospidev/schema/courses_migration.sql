-- Courses Module Schema Additions
-- Run this on your existing pidevjava database

-- Add recruiter_id to formation table
ALTER TABLE formation ADD COLUMN recruiter_id INT DEFAULT NULL;
ALTER TABLE formation ADD CONSTRAINT fk_formation_recruiter FOREIGN KEY (recruiter_id) REFERENCES users(id);

-- Add user_id to inscription table
ALTER TABLE inscription ADD COLUMN user_id INT DEFAULT NULL;
ALTER TABLE inscription ADD CONSTRAINT fk_inscription_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Modify tentative_quiz to use user_id instead of manual name
ALTER TABLE tentative_quiz ADD COLUMN user_id INT DEFAULT NULL;
ALTER TABLE tentative_quiz ADD CONSTRAINT fk_tentative_user FOREIGN KEY (user_id) REFERENCES users(id);
