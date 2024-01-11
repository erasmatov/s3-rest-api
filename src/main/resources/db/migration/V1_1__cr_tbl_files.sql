CREATE TABLE files
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename   VARCHAR(128)   NOT NULL UNIQUE,
    location   VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status     VARCHAR(8)
);