-- Autor: Rodriguez Alvarado Andersson Eduardo
-- Etapa 7: registro público, recuperación de clave y nuevos roles.
SET NAMES utf8mb4;
SET time_zone = '-05:00';
USE modaexpress;
-- Ejecutar una sola vez después de 04. Si Antigravity ya lo aplicó, no repetirlo.

-- Ampliar roles: DUENO (dueño de tienda), CLIENTE (comprador)
ALTER TABLE usuarios
  MODIFY rol ENUM('ADMIN','DUENO','VENDEDOR','CLIENTE') NOT NULL DEFAULT 'VENDEDOR';

-- Campo opcional para nombre de negocio (solo DUEÑO)
ALTER TABLE usuarios
  ADD nombre_negocio VARCHAR(150) NULL AFTER correo;

-- Campos para recuperación de clave (token temporal)
ALTER TABLE usuarios
  ADD token_recuperacion VARCHAR(64) NULL,
  ADD token_expira DATETIME NULL;
