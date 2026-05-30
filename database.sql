-- Royal Enfield Service Management System
-- Run in MySQL Workbench or: mysql -u root -p < database.sql

CREATE DATABASE IF NOT EXISTS re_service_db;
USE re_service_db;

CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(120),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    bike_model VARCHAR(80),
    service_type VARCHAR(80),
    status VARCHAR(40) DEFAULT 'Pending',
    amount DECIMAL(10,2) DEFAULT 0,
    service_date DATE,
    notes TEXT,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE
);
