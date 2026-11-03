<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Cómo ejecutar y probar ModaExpress Perú

## Estado entregado
Proyecto Ant en `/home/ander/NetBeansProjects/LoginJSP`, Java 17 y Tomcat 9 (`javax.servlet`). Usa el servicio MariaDB existente en `127.0.0.1:3306`. No se utiliza ni se conserva una instancia 3307 dentro del proyecto. Los scripts 01–05 ya fueron aplicados a esta base durante el trabajo; no volver a ejecutarlos sobre ella.

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

La primera orden compila con Java 17 usando las librerías del Tomcat configurado en NetBeans. `--bd` ejecuta PruebasSeguridad y PruebasVentas: PBKDF2, Singleton, CRUD, IGV, RUC, unicidad, rollback y dos peticiones simultáneas que disputan stock. Las pruebas de ventas crean y eliminan sus registros de prueba; pueden dejar saltos en correlativos e IDs, que no se reutilizan. No ejecutarlas en una base de producción.

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
