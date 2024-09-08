CREATE DATABASE atm_system;
USE atm_system;
CREATE TABLE users (
    account_number INT PRIMARY KEY,  -- Unique account number
    pin VARCHAR(4) NOT NULL,         -- 4-digit PIN
    balance DECIMAL(10, 2) DEFAULT 0.00  -- Account balance
);
INSERT INTO users (account_number, pin, balance) VALUES
(123456, '1234', 5000.00),  -- Sample user 1
(654321, '4321', 10000.00); 