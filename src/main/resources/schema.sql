CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(50) NOT NULL DEFAULT '21반',
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    nickname VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    tags VARCHAR(255),
    reported BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS class_flowers (
    class_name VARCHAR(50) PRIMARY KEY,
    flower_type VARCHAR(50) NOT NULL DEFAULT 'sunflower'
);

CREATE TABLE IF NOT EXISTS answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    nickname VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    adopted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_answers_question
        FOREIGN KEY (question_id)
        REFERENCES questions (id)
        ON DELETE CASCADE
);
