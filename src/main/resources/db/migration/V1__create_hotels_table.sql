CREATE TABLE hotels (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    distance_from_center INTEGER NOT NULL,
    rating NUMERIC(2,1) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0
);