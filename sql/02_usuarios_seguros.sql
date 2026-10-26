-- Autor: Rodriguez Alvarado Andersson Eduardo
-- Migración de etapa 2 a 3. Ejecutar una sola vez, en orden.
USE modaexpress;
ALTER TABLE usuarios CHANGE id id_usuario INT NOT NULL AUTO_INCREMENT,
    CHANGE clave clave_hash VARCHAR(255) NOT NULL,
    ADD nombres VARCHAR(100) NOT NULL DEFAULT '',
    ADD apellidos VARCHAR(100) NOT NULL DEFAULT '',
    ADD dni CHAR(8), ADD correo VARCHAR(150),
    ADD rol ENUM('ADMIN','VENDEDOR') NOT NULL DEFAULT 'VENDEDOR',
    ADD estado ENUM('ACTIVO','BLOQUEADO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    ADD intentos_fallidos INT NOT NULL DEFAULT 0,
    ADD fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD ultimo_acceso DATETIME NULL;
-- Conserva los identificadores y nombres de usuario; otras cuentas requieren cambio de clave.
UPDATE usuarios SET nombres='Usuario', apellidos='Migrado',
    dni=LPAD(id_usuario,8,'0'), correo=CONCAT('usuario',id_usuario,'@ejemplo.test'),
    estado='INACTIVO', clave_hash='120000:MmKDO9fvJiFagZ/4f0pSaA==:EsSAz0Z7iMhbgg/Gdtuv7J5vuBFVzk+A5zKALN5RE+I=';
UPDATE usuarios SET nombres='Administrador', apellidos='ModaExpress',
    dni='00000001', correo='admin@modaexpress.test', rol='ADMIN', estado='ACTIVO',
    clave_hash='120000:gbInga/pMxfry908rcm2FQ==:dNPyGls5Ky0RAGvBbmX0xVbsLHylGi7/VUAb7QFMVlA=' WHERE usuario='admin';
ALTER TABLE usuarios MODIFY dni CHAR(8) NOT NULL, MODIFY correo VARCHAR(150) NOT NULL,
    ADD CONSTRAINT uq_usuarios_dni UNIQUE(dni), ADD CONSTRAINT uq_usuarios_correo UNIQUE(correo),
    ADD CONSTRAINT ck_usuarios_dni CHECK(dni REGEXP '^[0-9]{8}$'),
    ADD CONSTRAINT ck_intentos CHECK(intentos_fallidos >= 0);
INSERT INTO usuarios(nombres,apellidos,dni,correo,usuario,clave_hash,rol)
VALUES('Lucía','Vargas','00000002','vendedor1@modaexpress.test','vendedor1','120000:wvMbkhmDOKySEB7NxGZ1XA==:kE9woWNTjOlT219jdPulHALl9OFIu/MiN+00lNC69gA=','VENDEDOR');
