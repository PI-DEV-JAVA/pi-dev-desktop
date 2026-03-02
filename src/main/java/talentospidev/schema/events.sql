-- Events Module Schema
-- Run this against your MySQL database

CREATE TABLE IF NOT EXISTS event (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type ENUM('MEETUP','CONFERENCE','WORKSHOP','WEBINAR') DEFAULT 'MEETUP',
    event_date DATETIME NOT NULL,
    end_date DATETIME,
    location VARCHAR(500),
    latitude DOUBLE,
    longitude DOUBLE,
    is_online BOOLEAN DEFAULT FALSE,
    online_link VARCHAR(500),
    max_capacity INT DEFAULT 0,
    cover_image VARCHAR(500),
    organizer_id INT NOT NULL,
    status ENUM('UPCOMING','ONGOING','COMPLETED','CANCELLED') DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS event_participation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    status ENUM('CONFIRMED','PENDING','CANCELLED','ATTENDED') DEFAULT 'PENDING',
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    qr_code VARCHAR(500),
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE KEY unique_participation (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS event_like (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE KEY unique_like (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS event_comment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
