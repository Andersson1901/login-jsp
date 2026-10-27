-- Autor: Rodriguez Alvarado Andersson Eduardo
SET NAMES utf8mb4;
SET time_zone = '-05:00';
-- Etapa 2: ejecutar una sola vez sobre una base nueva.
CREATE DATABASE IF NOT EXISTS modaexpress CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE modaexpress;
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Única etapa que conserva una contraseña de aplicación en texto plano.
INSERT INTO usuarios (usuario, clave) VALUES ('admin', '1234');
