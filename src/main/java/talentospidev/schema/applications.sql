CREATE TABLE applications (
    id                 INT PRIMARY KEY AUTO_INCREMENT,
    user_id            INT            NOT NULL,
    offer_id           INT            NOT NULL,
    cv_file_path       VARCHAR(500),
    motivation_letter  TEXT,
    status             VARCHAR(50)    DEFAULT 'Nouvelle',
    application_date   DATE           DEFAULT (CURRENT_DATE),
    score              DOUBLE         DEFAULT 0,
    notes              TEXT,
    interviewer        VARCHAR(150),
    interview_date     DATE,
    interview_result   VARCHAR(100),
    recruiter_response TEXT,
    response_date      DATE,

    UNIQUE KEY unique_user_offer (user_id, offer_id),
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE
);