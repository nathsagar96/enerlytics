CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(320) NOT NULL UNIQUE,
    address VARCHAR(500),
    alerting BOOLEAN NOT NULL DEFAULT FALSE,
    energy_alerting_threshold DOUBLE PRECISION NOT NULL DEFAULT 0
);
