CREATE TABLE IF NOT EXISTS profiles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  birth_date DATE,
  phone_number VARCHAR(50),
  location VARCHAR(255),
  professional_title VARCHAR(255),
  years_of_experience INT DEFAULT 0,
  summary TEXT,
  profile_completed BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
