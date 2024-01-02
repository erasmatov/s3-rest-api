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