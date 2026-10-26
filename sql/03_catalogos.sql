-- Autor: Rodriguez Alvarado Andersson Eduardo
USE modaexpress;
CREATE TABLE clientes (
 id_cliente INT AUTO_INCREMENT PRIMARY KEY,
 tipo_documento ENUM('DNI','RUC') NOT NULL, nro_documento VARCHAR(11) NOT NULL UNIQUE,
 nombres_razon_social VARCHAR(150) NOT NULL, direccion VARCHAR(200) NOT NULL,
 telefono VARCHAR(20) NOT NULL, correo VARCHAR(150) NOT NULL,
 estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
 CHECK ((tipo_documento='DNI' AND nro_documento REGEXP '^[0-9]{8}$') OR (tipo_documento='RUC' AND nro_documento REGEXP '^[0-9]{11}$'))
) ENGINE=InnoDB;
CREATE TABLE proveedores (
 id_proveedor INT AUTO_INCREMENT PRIMARY KEY, ruc CHAR(11) NOT NULL UNIQUE,
 razon_social VARCHAR(150) NOT NULL, contacto VARCHAR(100) NOT NULL,
 telefono VARCHAR(20) NOT NULL, correo VARCHAR(150) NOT NULL, direccion VARCHAR(200) NOT NULL,
 estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO', CHECK(ruc REGEXP '^[0-9]{11}$')
) ENGINE=InnoDB;
CREATE TABLE productos (
 id_producto INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(30) NOT NULL UNIQUE,
 nombre VARCHAR(150) NOT NULL, descripcion VARCHAR(500) NOT NULL,
 tipo ENUM('MERCADERIA','PRODUCTO','INSUMO','SERVICIO') NOT NULL,
 categoria VARCHAR(80) NOT NULL, unidad_medida VARCHAR(20) NOT NULL,
 precio_compra DECIMAL(12,2) NOT NULL, precio_venta DECIMAL(12,2) NOT NULL,
 stock DECIMAL(12,3) NOT NULL DEFAULT 0, stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0,
 estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
 CHECK(precio_compra >= 0 AND precio_venta >= 0 AND stock >= 0 AND stock_minimo >= 0),
 CHECK(tipo <> 'SERVICIO' OR (stock=0 AND stock_minimo=0))
) ENGINE=InnoDB;
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('DNI','10000001','Ana Torres','Av. Primavera 100, Lima','900100001','cliente1@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('DNI','10000002','Luis Rojas','Av. Primavera 200, Lima','900100002','cliente2@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('DNI','10000003','María Quispe','Av. Primavera 300, Lima','900100003','cliente3@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('DNI','10000004','Pedro Flores','Av. Primavera 400, Lima','900100004','cliente4@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('RUC','20100000005','Boutique Primavera SAC','Av. Primavera 500, Lima','900100005','cliente5@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('RUC','20100000006','Uniformes del Sur SAC','Av. Primavera 600, Lima','900100006','cliente6@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('RUC','20100000007','Moda Juvenil EIRL','Av. Primavera 700, Lima','900100007','cliente7@ejemplo.test');
INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo) VALUES('RUC','20100000008','Textiles Lima SAC','Av. Primavera 800, Lima','900100008','cliente8@ejemplo.test');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000001','Algodones del Perú SAC','Contacto textil 1','900200001','proveedor1@ejemplo.test','Jr. Comercio 120, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000002','Textiles Gamarra SAC','Contacto textil 2','900200002','proveedor2@ejemplo.test','Jr. Comercio 240, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000003','Denim Andino EIRL','Contacto textil 3','900200003','proveedor3@ejemplo.test','Jr. Comercio 360, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000004','Botones y Cierres SAC','Contacto textil 4','900200004','proveedor4@ejemplo.test','Jr. Comercio 480, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000005','Lanas del Cusco SAC','Contacto textil 5','900200005','proveedor5@ejemplo.test','Jr. Comercio 600, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000006','Confecciones Norte SAC','Contacto textil 6','900200006','proveedor6@ejemplo.test','Jr. Comercio 720, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000007','Calzado Urbano SAC','Contacto textil 7','900200007','proveedor7@ejemplo.test','Jr. Comercio 840, Lima');
INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion) VALUES('20200000008','Empaques Eco Perú SAC','Contacto textil 8','900200008','proveedor8@ejemplo.test','Jr. Comercio 960, Lima');
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('POL-001','Polo algodón blanco','Polo unisex talla M','MERCADERIA','Polos','UND',18,35,80,10);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('JEA-001','Jean azul clásico','Jean recto talla 32','MERCADERIA','Pantalones','UND',45,85,40,5);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('CAS-001','Casaca denim','Casaca urbana talla L','PRODUCTO','Casacas','UND',60,120,20,4);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('VES-001','Vestido floral','Vestido de verano talla M','PRODUCTO','Vestidos','UND',40,79,30,5);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('TEL-001','Tela algodón','Tela para confección por metro','INSUMO','Telas','METRO',8,14,150,20);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('HIL-001','Hilo de costura','Cono de hilo negro','INSUMO','Costura','CONO',4,8,60,10);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('SER-001','Ajuste de basta','Servicio de basta de pantalón','SERVICIO','Arreglos','SERVICIO',0,15,0,0);
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo) VALUES('SER-002','Bordado personalizado','Servicio de bordado en prenda','SERVICIO','Personalización','SERVICIO',0,25,0,0);
