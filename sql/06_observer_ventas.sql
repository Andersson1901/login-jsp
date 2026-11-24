-- Autor: Rodriguez Alvarado Andersson Eduardo
SET NAMES utf8mb4;
SET time_zone = '-05:00';
USE modaexpress;
-- Etapa 8: patrón Observer. Avisos al dueño de tienda cuando cambia el estado de una venta.
-- Ejecutar una sola vez, después de 05. Si la venta se elimina, el aviso se conserva sin enlace.
CREATE TABLE notificacion (
 id_notificacion INT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL, id_venta INT NULL,
 nro_venta VARCHAR(30) NOT NULL, cliente VARCHAR(150) NOT NULL,
 estado_anterior ENUM('EMITIDA','ANULADA') NULL, estado_nuevo ENUM('EMITIDA','ANULADA') NOT NULL,
 total DECIMAL(14,2) NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 leida TINYINT(1) NOT NULL DEFAULT 0,
 FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
 FOREIGN KEY(id_venta) REFERENCES venta(id_venta) ON DELETE SET NULL,
 INDEX ix_notificacion_usuario(id_usuario,leida,fecha)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
