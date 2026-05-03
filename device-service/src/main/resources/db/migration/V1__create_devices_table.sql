CREATE TABLE devices
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    type     VARCHAR(50)  NOT NULL,
    location VARCHAR(255) NOT NULL,
    user_id  BIGINT       NOT NULL
);
