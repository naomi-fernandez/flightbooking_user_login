CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    verification_code VARCHAR(10),
    verification_expiration DATETIME,
    enabled BOOLEAN DEFAULT FALSE,
    one_time_password VARCHAR(10),
    otp_requested_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);



