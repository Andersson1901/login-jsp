<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Cómo ejecutar y probar ModaExpress Perú

## Estado entregado
Proyecto Ant en `/home/ander/NetBeansProjects/LoginJSP`, Java 17 y Tomcat 9 (`javax.servlet`). Usa el servicio MariaDB existente en `127.0.0.1:3306`. No se utiliza ni se conserva una instancia 3307 dentro del proyecto. Los scripts 01–05 ya fueron aplicados a esta base durante el trabajo; no volver a ejecutarlos sobre ella. El script 06 (Etapa 8, Observer de ventas) es nuevo: aplícalo una sola vez antes de ejecutar la aplicación.

## NetBeans
1. Abre el proyecto LoginJSP.
2. En Propiedades selecciona la plataforma Java 17 y Apache Tomcat 9.
3. Ejecuta **Limpiar y construir**, después **Ejecutar**.
4. Abre `http://localhost:8080/LoginJSP/` (si cambiaste el puerto de Tomcat, ajusta la URL).
5. Usuarios de ejemplo: `admin / 1234` y `vendedor1 / 1234`. Ambos quedan ACTIVO con cero intentos fallidos. Solo ADMIN administra usuarios.

No necesitas registrar JAR manualmente: MariaDB Connector/J 3.5.10 y JSTL 1.2 javax están en `web/WEB-INF/lib/`. SB Admin 7.0.7/Bootstrap 5 y su licencia MIT están en assets; no dependen de CDN.

## Crear otra base desde cero
Usa una base de desarrollo vacía. El archivo `sql/00_cuenta_desarrollo.sql` es opcional y requiere una cuenta administradora para crear la base y el usuario. Si ya existen, omítelo. Los scripts siguientes se ejecutan **una sola vez y en este orden**, con los permisos de migración que preparaste:

```fish
cd ~/NetBeansProjects/LoginJSP
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/01_usuarios_inicial.sql
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/02_usuarios_seguros.sql
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/03_catalogos.sql
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/04_ventas.sql
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/05_registro_publico.sql
mariadb --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u moda_dev -p < sql/06_observer_ventas.sql
```

La única contraseña de aplicación en texto plano está en 01, como pide la etapa 2; 02 la sustituye por PBKDF2. Los cambios de Git no revierten la base: para probar un commit antiguo, usa una base nueva con los scripts correspondientes a esa etapa. No elimines datos actuales para volver atrás.

## Conexión privada y cambio de contraseña
`src/conexion.properties` está ignorado por Git. El archivo `.example` contiene únicamente valores ficticios de desarrollo:

```properties
url=jdbc:mariadb://127.0.0.1:3306/modaexpress?useUnicode=true&characterEncoding=UTF-8
usuario=moda_dev
clave=ModaPrueba2026
```

Si falta el archivo, ConexionBD usa esos valores de desarrollo. Para cambiar la clave de BD, el administrador debe ejecutar `ALTER USER 'moda_dev'@'localhost' IDENTIFIED BY 'TU_NUEVA_CLAVE';` usando el host real de la cuenta (compruébalo con `SELECT CURRENT_USER();`; en esta máquina fue `moda_dev@localhost`). Después actualiza solo `src/conexion.properties`, reconstruye y vuelve a desplegar. Nunca guardes una clave real en SQL ni en Git. La cuenta necesita DDL para migraciones; el uso normal de la web solo necesita SELECT, INSERT, UPDATE y DELETE.

## Recorrido manual
- Acceso: falla dos veces con vendedor1; acierta y comprueba que el contador vuelve a cero. Falla tres veces: aparece bloqueado.jsp, se invalida la sesión y tampoco entra con la clave correcta. Admin puede desbloquearlo en Usuarios.
- Usuarios: buscar, crear, editar, cambiar clave, desbloquear y eliminar otro usuario. El sistema rechaza DNI/correo/usuario duplicados y eliminar la cuenta actual.
- Clientes y proveedores: prueba documentos válidos e inválidos, búsqueda, edición y eliminación con confirmación.
- Productos: prueba precios negativos y duplicados de código. SERVICIO guarda stock y stock mínimo cero. No se puede cambiar el tipo de un producto que ya tiene pedidos.
- Ventas: crea una SOLPED, agrega y quita ítems, guarda y aprueba. Convierte en venta; comprueba descuento de stock, subtotal + IGV = total y SOLPED ATENDIDA.
- Usa un servicio para comprobar que no descuenta stock. Prueba cantidad superior al stock: no se debe guardar ninguna parte de la venta.
- Una factura requiere cliente RUC; una boleta admite DNI. Emite el comprobante y luego la guía. Se permite como máximo uno de cada documento por venta.
- Edición: SOLPED pendiente; venta emitida sin documentos; comprobante sin guía; guía mientras la venta esté emitida. Cambiar el tipo del comprobante cambia de serie y asigna otro correlativo.
- Baja: anula la venta para devolver stock una sola vez. Para eliminarla, elimina primero guía y comprobante; queda la SOLPED aprobada para corregir o reutilizar. Es un flujo académico, no una anulación tributaria.

## Pruebas automatizadas
Desde la carpeta del proyecto, en fish:

```fish
python3 herramientas/verificar.py
python3 herramientas/verificar.py --bd
```

La primera orden compila con Java 17 usando las librerías del Tomcat configurado en NetBeans. `--bd` ejecuta PruebasSeguridad, PruebasVentas, PruebasObservador (requiere el script 06) y PruebasReporte: PBKDF2, Singleton, CRUD, IGV, RUC, unicidad, rollback y dos peticiones simultáneas que disputan stock. Las pruebas de ventas crean y eliminan sus registros de prueba; pueden dejar saltos en correlativos e IDs, que no se reutilizan. No ejecutarlas en una base de producción.

También se probaron por HTTP las 25 vistas JSP, roles, CSRF, escape HTML, bloqueo y los cuatro CRUD más el proceso de ventas. Se verificaron visualmente las tildes, la cabecera compartida y agregar/quitar ítems en navegador. Los JSP tienen pageEncoding UTF-8 y cada página HTML tiene meta charset UTF-8 a través de su head; los includes de cuerpo no duplican la etiqueta meta.

## Límites y supuestos
Se usaron los campos de usuarios por defecto del enunciado. Importes con dos decimales y cantidades con tres para admitir telas por metro. Datos ficticios del rubro ropa: ocho clientes, ocho proveedores, ocho productos y ejemplos de pedidos, venta, boleta y guía. No hay integración SUNAT ni validez tributaria.

Singleton administra la configuración; **cada llamada obtiene una Connection nueva**. Compartir una sola Connection entre peticiones mezclaría transacciones y cierres. No hay pool de conexiones ni paginación, adecuados como mejoras futuras.

El contador de nombres inexistentes vive en la sesión y puede reiniciarse al borrar cookies. `window.close()` depende del navegador y normalmente solo cierra ventanas abiertas por script; si no puede, se muestra «Ya puedes cerrar esta pestaña».

## Render, sin modificarlo
El Dockerfile original solo copia web/. Para publicar habría que compilar las clases Java 17 con las librerías de Tomcat, copiar el resultado a WEB-INF/classes, incluir los JAR de WEB-INF/lib y configurar una MariaDB accesible desde Render con credenciales privadas y TLS. No incluir el archivo privado de desarrollo en una imagen pública. No se modificó Dockerfile ni se hizo git push.


## Etapa 7: registro público, recuperación simulada y catálogo
Desde el login están disponibles los tres registros, la recuperación y el catálogo. Comprador crea CLIENTE; tienda crea DUENO y exige nombre del negocio; vendedor crea VENDEDOR. Todos quedan ACTIVO sin aprobación. No se permite crear ADMIN desde el registro público.

CLIENTE inicia sesión en el catálogo y no puede abrir ni modificar usuarios, clientes, proveedores, productos o ventas. DUENO y VENDEDOR usan el panel operativo compartido; solo ADMIN administra usuarios y puede desactivar cualquiera de las cuentas. El nombre de negocio es un dato de la cuenta: esta etapa no crea inventarios separados por tienda ni compras en línea.

Recuperación: introduce un correo válido en recuperar.jsp. Si pertenece a una cuenta ACTIVO, se guarda un token aleatorio protegido con SHA-256 y una caducidad de 15 minutos. El mensaje es igual si el correo no existe. Es una simulación: no se envía correo, no se expone el token y no se cambia la contraseña. El cambio real de clave continúa disponible para ADMIN en Usuarios.

Catálogo: `/catalogo` o `/catalogo.jsp`, sin sesión. Muestra solo productos activos, búsqueda, precios sin IGV y disponibilidad. No expone precios de compra ni ofrece operaciones de escritura.

Prueba automatizada adicional (requiere Tomcat iniciado, cliente mariadb y la BD de desarrollo configurada):

```fish
python3 test/PruebasPublicas.py http://localhost:8080/LoginJSP/
```

Esta prueba usa admin/1234, registra cuentas ficticias, comprueba validaciones, hashes, permisos, desactivación, CSRF, recuperación y catálogo; elimina sus registros al terminar. No usa ni envía correos reales. La base actual ya tenía el script 05 aplicado por Antigravity; no hay que repetirlo.


## Etapa 8: Observer de ventas para el dueño de tienda
Aplica `sql/06_observer_ventas.sql` una sola vez, reconstruye y vuelve a desplegar. Con una cuenta DUENO ACTIVO (créala desde registro_tienda.jsp o desde Usuarios como ADMIN):

- Con otra sesión (admin o vendedor1), emite una venta desde una SOLPED aprobada y luego anúlala. El dueño ve, en el dashboard, «Tienes N aviso(s) de ventas sin leer» y, en el menú Notificaciones, un aviso «Venta emitida» y otro «EMITIDA → ANULADA», ambos marcados como Nueva.
- «Marcar todas como leídas» quita las marcas y el aviso del dashboard.
- Una venta rechazada (por ejemplo, stock insuficiente) no genera avisos.
- Si se elimina una venta anulada, sus avisos permanecen sin el botón «Ver venta».
- Un usuario ADMIN, VENDEDOR o CLIENTE que abra `/notificaciones` recibe 403. Una cuenta DUENO INACTIVO no recibe avisos.

Prueba automatizada: `python3 herramientas/verificar.py --bd` ejecuta también PruebasObservador. La primera parte no usa la base; la segunda crea tres usuarios, un cliente y un producto de prueba, comprueba los avisos y los elimina al terminar. `java -cp <clases> PruebasObservador --sin-bd` ejecuta solo la primera parte.

Límites: los avisos son síncronos y de mejor esfuerzo (si un observador falla solo queda un registro en el log), no se envían correos y no hay separación de avisos por tienda. Se listan los últimos 100.


## Etapa 9: Reportería de ventas
No requiere script SQL. Reconstruye y vuelve a desplegar. Con la base de desarrollo en marcha:

- Entra como vendedor1 y pulsa «Reporte de ventas» en el inicio. Se muestran los últimos 30 días con sus pedidos y ventas; no aparece selector de vendedor. Cambiar `?vendedor=` en la dirección no muestra datos de otro vendedor.
- Entra como admin o como un DUENO: aparece el selector. «Toda la tienda» suma a todos los vendedores; al elegir uno, las cifras coinciden con las de esa persona.
- Emite una venta y anúlala: la anulada aparece en la nota inferior y no suma a los ingresos. La emitida aparece en las tablas de ventas por día, productos y clientes, y en los gráficos.
- «Descargar Excel» entrega `reporte-ventas-<desde>_<hasta>.xlsx` con las hojas Resumen, Ventas por día, Productos, Clientes, Ventas y Pedidos; sus cifras coinciden con la pantalla.
- Fechas inválidas, inicial posterior a la final o un rango de más de 366 días muestran un mensaje y no consultan la base. Un rango sin datos muestra ceros y «Sin datos en el rango».
- Un usuario CLIENTE que abra `/reportes` recibe 403.

Prueba automatizada: `python3 herramientas/verificar.py --bd` ejecuta también PruebasReporte. La primera parte no usa la base; la segunda crea dos vendedores, un cliente y un producto de prueba, comprueba las cifras de cada vendedor y de la tienda, y los elimina al terminar. `java -cp build/verificacion PruebasReporte --sin-bd` ejecuta solo la primera parte.

Límites: no hay separación por tienda; los rankings muestran 10 filas y el detalle del Excel hasta 5000 por consulta; solo se informan los datos que el sistema ya registra.


## Etapa 10: Paginación, orden por columna y búsqueda automática
Ejecuta una sola vez `sql/07_paginacion.sql` (índices) y vuelve a desplegar. Para probar con muchos registros puedes cargar 10 000 productos de prueba (usa el motor SEQUENCE de MariaDB; no se ejecutó en este entorno) y borrarlos al terminar:

```sql
INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo,estado)
SELECT CONCAT('PRB-',LPAD(seq,5,'0')),CONCAT('Producto de prueba ',seq),'prueba','MERCADERIA','PruebaPaginacion','UND',1,seq/10,5,0,'ACTIVO' FROM seq_1_to_10000;
-- al terminar: DELETE FROM productos WHERE categoria='PruebaPaginacion';
```

- Productos: la tabla muestra 10 filas y el pie dice «Página 1 de 10 · filas 1 a 10 de 100 traídas (tope alcanzado…)». Con el selector «Filas traídas de la BD» en 500 hay 50 páginas; en 50, cinco.
- Pulsa «Precio sin IGV»: ordena de forma descendente (▼) y la página vuelve a 1. Púlsalo otra vez: ascendente (▲). Pulsa «Nombre»: empieza en descendente. Cambiar de página conserva el orden, el límite y la búsqueda.
- Escribe `Codigo: PRB-0010` y espera medio segundo sin pulsar nada: la tabla se actualiza sola y la dirección cambia. Mientras no cambies el texto, no se envían más pedidos (pestaña Red del navegador). Con el campo vacío no se envía nada; el botón Buscar con el campo vacío muestra el listado completo.
- `PRECIO SIN IGV: 3`, `categoría: prueba` y `Codigo: prb-0010` funcionan igual (primera palabra, sin distinguir mayúsculas ni tildes). `Marca: x` muestra «Columna no reconocida. Usa: …» y no consulta. `Nombre: 50%` busca el texto «50%», no «todo».
- Repite el orden, el tope y las páginas en clientes, proveedores, usuarios, SOLPED, ventas, comprobantes, guías y avisos. Los permisos no cambian: un CLIENTE solo ve el catálogo.
- Catálogo público (sin iniciar sesión): páginas de 10, búsqueda automática y `Código`, `Nombre` y `Categoría` como columnas válidas; `Estado: x` muestra el aviso.
- Reportes: aparecen «Detalle de ventas» y «Detalle de pedidos» con orden, tope y páginas propios; ordenar uno no cambia el otro ni el rango de fechas, y el Excel sigue igual.

Prueba automatizada: `python3 herramientas/verificar.py --bd` ejecuta también PruebasPaginacion. La primera parte no usa la base; la segunda crea 120 productos de prueba, comprueba el tope, las páginas, el orden y los filtros, y los elimina al terminar. `java -cp build/verificacion PruebasPaginacion --sin-bd` ejecuta solo la primera parte.

Límites: los formularios de pedido siguen cargando todos los clientes y productos en sus listas desplegables; los productos no se separan por negocio; los filtros `LIKE '%valor%'` no usan índices, y lo que acota su costo es el tope de filas traídas; el total que se muestra llega como máximo al tope elegido.
