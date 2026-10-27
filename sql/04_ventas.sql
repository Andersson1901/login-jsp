-- Autor: Rodriguez Alvarado Andersson Eduardo
SET NAMES utf8mb4;
SET time_zone = '-05:00';
USE modaexpress;
CREATE TABLE correlativos (serie VARCHAR(10) PRIMARY KEY, ultimo INT NOT NULL DEFAULT 0) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE solped (
 id_solped INT AUTO_INCREMENT PRIMARY KEY, nro_solped VARCHAR(30) NOT NULL UNIQUE,
 id_cliente INT NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado ENUM('PENDIENTE','APROBADA','ATENDIDA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
 observacion VARCHAR(500) NOT NULL DEFAULT '', id_usuario INT NOT NULL,
 FOREIGN KEY(id_cliente) REFERENCES clientes(id_cliente), FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE solped_detalle (
 id INT AUTO_INCREMENT PRIMARY KEY, id_solped INT NOT NULL, id_producto INT NOT NULL,
 cantidad DECIMAL(12,3) NOT NULL, precio_unitario DECIMAL(12,2) NOT NULL, subtotal DECIMAL(14,2) NOT NULL,
 FOREIGN KEY(id_solped) REFERENCES solped(id_solped) ON DELETE CASCADE,
 FOREIGN KEY(id_producto) REFERENCES productos(id_producto), UNIQUE(id_solped,id_producto),
 CHECK(cantidad>0 AND precio_unitario>=0 AND subtotal>=0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE venta (
 id_venta INT AUTO_INCREMENT PRIMARY KEY, nro_venta VARCHAR(30) NOT NULL UNIQUE,
 id_solped INT NOT NULL UNIQUE, id_cliente INT NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 subtotal DECIMAL(14,2) NOT NULL, igv DECIMAL(14,2) NOT NULL, total DECIMAL(14,2) NOT NULL,
 estado ENUM('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA', id_usuario INT NOT NULL,
 FOREIGN KEY(id_solped) REFERENCES solped(id_solped), FOREIGN KEY(id_cliente) REFERENCES clientes(id_cliente),
 FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario), CHECK(subtotal>=0 AND igv>=0 AND total=subtotal+igv)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE venta_detalle (
 id INT AUTO_INCREMENT PRIMARY KEY, id_venta INT NOT NULL, id_producto INT NOT NULL,
 cantidad DECIMAL(12,3) NOT NULL, precio_unitario DECIMAL(12,2) NOT NULL, subtotal DECIMAL(14,2) NOT NULL,
 FOREIGN KEY(id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE,
 FOREIGN KEY(id_producto) REFERENCES productos(id_producto), UNIQUE(id_venta,id_producto),
 CHECK(cantidad>0 AND precio_unitario>=0 AND subtotal>=0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE comprobante (
 id_comprobante INT AUTO_INCREMENT PRIMARY KEY, tipo ENUM('FACTURA','BOLETA') NOT NULL,
 serie CHAR(4) NOT NULL, numero INT NOT NULL, id_venta INT NOT NULL UNIQUE,
 fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, total DECIMAL(14,2) NOT NULL,
 FOREIGN KEY(id_venta) REFERENCES venta(id_venta), UNIQUE(serie,numero),
 CHECK((tipo='FACTURA' AND serie='F001') OR (tipo='BOLETA' AND serie='B001')),
 CHECK(numero>0 AND total>=0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE guia_remision (
 id_guia INT AUTO_INCREMENT PRIMARY KEY, serie CHAR(4) NOT NULL DEFAULT 'T001', numero INT NOT NULL,
 id_venta INT NOT NULL UNIQUE, fecha_traslado DATE NOT NULL, punto_partida VARCHAR(200) NOT NULL,
 punto_llegada VARCHAR(200) NOT NULL, estado ENUM('PENDIENTE','ENTREGADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
 FOREIGN KEY(id_venta) REFERENCES venta(id_venta), UNIQUE(serie,numero), CHECK(serie='T001' AND numero>0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Ejemplos académicos: importes sin IGV y movimiento de stock consistente.
START TRANSACTION;
INSERT INTO correlativos VALUES ('SP',3),('V',1),('F001',0),('B001',1),('T001',1);
INSERT INTO solped(id_solped,nro_solped,id_cliente,estado,observacion,id_usuario)
VALUES (1,'SP-000001',2,'PENDIENTE','Pedido de polos para uso personal',1),
       (2,'SP-000002',5,'APROBADA','Pedido de boutique, pendiente de venta',1),
       (3,'SP-000003',1,'ATENDIDA','Ejemplo de venta y entrega',1);
INSERT INTO solped_detalle(id_solped,id_producto,cantidad,precio_unitario,subtotal)
VALUES(1,1,3,35,105),(2,2,2,85,170),(2,8,1,25,25),(3,1,2,35,70),(3,7,1,15,15);
INSERT INTO venta(id_venta,nro_venta,id_solped,id_cliente,subtotal,igv,total,id_usuario)
VALUES(1,'V-000001',3,1,85,15.30,100.30,1);
INSERT INTO venta_detalle(id_venta,id_producto,cantidad,precio_unitario,subtotal)
VALUES(1,1,2,35,70),(1,7,1,15,15);
UPDATE productos SET stock=stock-2 WHERE id_producto=1;
INSERT INTO comprobante(tipo,serie,numero,id_venta,total) VALUES('BOLETA','B001',1,1,100.30);
INSERT INTO guia_remision(serie,numero,id_venta,fecha_traslado,punto_partida,punto_llegada,estado)
VALUES('T001',1,1,CURRENT_DATE,'Tienda ModaExpress, Lima','Av. Primavera 100, Lima','ENTREGADA');
COMMIT;
