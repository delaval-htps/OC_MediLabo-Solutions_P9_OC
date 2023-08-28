DROP TABLE IF EXISTS patient;
CREATE TABLE IF NOT EXISTS patient(
    id INT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(25),
    first_name VARCHAR(25),
    date_of_birth DATE,
    genre CHAR,
    patient_address VARCHAR(25),
    phone_number VARCHAR(25),
    primary key (id)
);