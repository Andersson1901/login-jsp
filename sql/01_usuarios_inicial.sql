-- Autor: Rodriguez Alvarado Andersson Eduardo
-- Etapa 2: ejecutar una sola vez sobre una base nueva.
CREATE DATABASE IF NOT EXISTS modaexpress CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE modaexpress;
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL
) ENGINE=InnoDB;
-- Única etapa que conserva una contraseña de aplicación en texto plano.
INSERT INTO usuarios (usuario, clave) VALUES ('admin', '1234');

CREATE USER IF NOT EXISTS 'moda_dev'@'127.0.0.1' IDENTIFIED BY PASSWORD '*66FD16CE98966CB770264CB25EE75FD6F5C7C67C';
GRANT SELECT, INSERT, UPDATE, DELETE ON modaexpress.* TO 'moda_dev'@'127.0.0.1';
