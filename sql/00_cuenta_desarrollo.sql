-- Autor: Rodriguez Alvarado Andersson Eduardo
SET NAMES utf8mb4;
SET time_zone = '-05:00';
-- Opcional: ejecutar con una cuenta administradora del servicio existente.
-- Si ya creaste el usuario y la base, omitir este archivo.
CREATE DATABASE IF NOT EXISTS modaexpress CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'moda_dev'@'127.0.0.1' IDENTIFIED BY PASSWORD '*66FD16CE98966CB770264CB25EE75FD6F5C7C67C';
GRANT ALL PRIVILEGES ON modaexpress.* TO 'moda_dev'@'127.0.0.1';
