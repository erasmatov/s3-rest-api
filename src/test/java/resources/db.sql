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

CREATE TABLE files
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename   VARCHAR(128)   NOT NULL UNIQUE,
    location   VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status     VARCHAR(8)
);

CREATE TABLE events
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    file_id    BIGINT,
    created_at TIMESTAMP,
    status     VARCHAR(8),

    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);