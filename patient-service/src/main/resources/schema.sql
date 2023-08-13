CREATE DATABASE IF NOT EXISTS patient_service;
USE patient_service;
CREATE TABLE IF NOT EXISTS patient(
    id INT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(25),
    first_name VARCHAR(25),
    date_of_birth TIMESTAMP,
    genre CHAR,
    patient_address VARCHAR(25),
    phone_number VARCHAR(25),
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;