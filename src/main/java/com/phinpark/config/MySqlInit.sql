CREATE DATABASE IF NOT EXISTS phinparks;
USE phinparks;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    account_type VARCHAR(255) NOT NULL DEFAULT 'CLIENT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS registered_vehicle (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id INT NOT NULL,
    vehicle_type VARCHAR(255) NOT NULL,
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    brand VARCHAR(255),
    model VARCHAR(255),
    color VARCHAR(255),
    registration_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_owner_id (owner_id),
    INDEX idx_plate_number (plate_number)
);
