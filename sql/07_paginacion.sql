-- Autor: Rodriguez Alvarado Andersson Eduardo
SET NAMES utf8mb4;
SET time_zone = '-05:00';
USE modaexpress;
-- Etapa 10: paginación y orden por columna. Índices en las columnas que más se ordenan.
-- Ejecutar una sola vez, después de 06. No cambia tablas ni datos. Los filtros "Columna: valor" usan LIKE '%valor%',
-- que no aprovecha estos índices; lo que acota ese costo es el tope de filas traídas.
CREATE INDEX ix_productos_nombre ON productos(nombre);
CREATE INDEX ix_productos_categoria ON productos(categoria);
CREATE INDEX ix_clientes_nombres ON clientes(nombres_razon_social);
CREATE INDEX ix_solped_fecha ON solped(fecha);
CREATE INDEX ix_venta_fecha ON venta(fecha);
CREATE INDEX ix_notificacion_fecha ON notificacion(id_usuario,fecha);
