CREATE TABLE offers (
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    recruiter_id          INT            NOT NULL,
    title                 VARCHAR(255)   NOT NULL,
    description           TEXT,
    department            VARCHAR(100),
    contract_type         VARCHAR(50),
    experience_level      VARCHAR(50),
    salary_min            DOUBLE,
    salary_max            DOUBLE,
    location              VARCHAR(150),
    status                VARCHAR(50),
    publish_date          DATE,
    closing_date          DATE,
    positions_available   INT            DEFAULT 1,
    applications_received INT            DEFAULT 0,

    FOREIGN KEY (recruiter_id) REFERENCES users(id) ON DELETE CASCADE
);
