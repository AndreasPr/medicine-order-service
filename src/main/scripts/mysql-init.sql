DROP DATABASE IF EXISTS medicineorderservice;
DROP USER IF EXISTS `medicine_order_service`@`%`;
CREATE DATABASE IF NOT EXISTS medicineorderservice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS `medicine_order_service`@`%` IDENTIFIED WITH mysql_native_password BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW,
CREATE ROUTINE, ALTER ROUTINE, EVENT, TRIGGER ON `medicineorderservice`.* TO `medicine_order_service`@`%`;
FLUSH PRIVILEGES;
