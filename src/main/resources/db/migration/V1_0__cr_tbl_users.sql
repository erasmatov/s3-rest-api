CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(32)   NOT NULL UNIQUE,
    password   VARCHAR(2048) NOT NULL,
    role       VARCHAR(16)   NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status     VARCHAR(8)
);