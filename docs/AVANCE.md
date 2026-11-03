<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Avance de ModaExpress Perú

## Lectura inicial
Se leyeron todos los archivos fuente, vistas, configuración Ant/NetBeans y archivos generados existentes antes de modificar el proyecto. Login original confirmado: admin / 1234, sesión usuario, inicio y cierre. Git estaba limpio en 1d2dff9. Dockerfile se conserva byte por byte.

## Supuestos
- Se usan los campos por defecto de usuarios porque la hoja no fue adjuntada.
- Java 17 instalado; se corrige la configuración anterior Java 21 y src/java (inexistente) a src.
- Comentario de autor en primera línea con sintaxis válida por formato: // Java/JS, JSP <%-- --%>, SQL --, properties/Python #, CSS /* */, Markdown/XML <!-- -->. Binarios y bibliotecas de terceros conservan su autoría y licencia originales.
- MariaDB y Connector/J 3.5.10 ya instalados. No se instalan paquetes ni se usa sudo.
- Se trabaja con commits locales; no se publica ni se cambia Dockerfile.

## Etapa 2
- Conexión JDBC configurable y main; login con PreparedStatement y recursos cerrados. Filter central y escape HTML.
- Script 01 crea la base, tabla, admin y cuenta limitada de desarrollo. JAR JDBC local incluido.
- La instancia del servicio existente exige autenticación administrativa. Se prepara instancia aislada en 3307, dentro de .local (ignorada por Git), con herramientas/bd_local.py. No altera el servicio existente.
- Compilación de las tres clases con javac 17 correcta. Pruebas HTTP y conexión pendientes antes del commit.


Etapa 2 verificada: conexión JDBC correcta a modaexpress; pruebas HTTP en Tomcat 9/Java 17 de filtro, clave incorrecta, login admin, sesión y logout. Compilación javac 17 correcta.

## Etapa 3: Singleton
Constructor privado y contenedor estático seguro para hilos. Configuración única por classloader, Connection nueva por petición. Login actualizado y análisis con fragmentos reales. Compilación Java 17 correcta; main JDBC verificado antes del commit.

## Etapa 3: validaciones
Migración 02 conserva el ID y usuario admin y añade vendedor1 con hash PBKDF2 (120000 iteraciones, sal de 16 bytes). Login transaccional con bloqueo de fila: 3 fallos, reinicio del contador al acertar, rechazo de cuentas bloqueadas/inactivas. Contador adicional en sesión para nombres inexistentes. Sesión nueva al acceder, invalidación al bloquear.
Verificación: javac 17; HTTP y BD para acierto/error, contador, bloqueo, inactivo, desconocido e intento de inyección SQL; pruebas de hash, sal, Singleton y conexiones distintas correctas.
Limitaciones: window.close solo funciona si el navegador permite cerrar esa ventana; se muestra «Ya puedes cerrar esta pestaña». El contador de desconocidos es por sesión y puede reiniciarse al borrar cookies; no equivale a una defensa distribuida. Datos migrados fuera de admin quedan inactivos hasta revisión.

## Etapa 4a: CRUD usuarios sin Repository
SB Admin 7.0.7 con Bootstrap 5 y licencia MIT local, JSTL 1.2 javax incluido. Layout compartido, dashboard, menú y barra de sesión con colores originales. UsuarioServlet contiene JDBC directo: búsqueda, alta, edición, baja, desbloqueo y clave. Restricciones únicas en BD, validaciones servidor, ADMIN para usuarios, protección CSRF y escape JSTL. Cierre de sesión ahora requiere POST.
Pruebas: Java 17 compila; Tomcat compila todas las vistas. Pruebas HTTP/BD del CRUD, duplicados, claves, desbloqueo, autoeliminación, XSS, roles, CSRF y logout aprobadas. Los módulos siguientes se implementan en sus etapas, en orden.

## Etapa 4b: Repository usuarios
Se extraen Repository<T, ID>, UsuarioRepository y UsuarioRepositoryJDBC. La fábrica Repositorios concentra la selección de JDBC. Servlet y login usan la interfaz; las consultas salen del servlet. Filter vuelve a comprobar el estado y rol en cada solicitud para revocar sesiones de cuentas bloqueadas, inactivas o eliminadas. Se agrega análisis breve con fragmentos reales.
Pruebas: compilación Java 17 y repetición del CRUD completo HTTP/BD correctas después de la refactorización.

## Etapa 5
Tablas clientes, proveedores y productos con 8 registros cada una en script 03. Modelos, interfaces Repository, implementaciones JDBC, servlets y vistas CRUD completas con búsqueda y confirmación de baja. Validación servidor de DNI/RUC, correo, importes y stock no negativos; restricciones de BD. Los servicios fuerzan stock y stock mínimo a cero. BigDecimal para importes (2 decimales) y existencias (3 decimales), para admitir insumos por metro.
Dashboard obtiene sus contadores a través de ResumenRepository. Documentación de patrones en analisis_repository_tablas.md.
Pruebas: 27 clases compilan en Java 17; HTTP/BD de los tres catálogos, duplicados, edición, búsquedas, documentos inválidos, negativos, servicios y bajas aprobadas.

## Preferencia confirmada durante la etapa 6 (registro histórico)
El usuario eligió el servicio MariaDB existente en 3306 y posteriormente confirmó que preparó la cuenta de desarrollo. Se cambia la URL por defecto y el ejemplo a 3306. Las verificaciones previas de etapas 2–5 se hicieron en 3307 mientras se esperaba la respuesta; se detuvo esa instancia. La limpieza final posterior retiró sus archivos y el helper del proyecto, como se registra abajo. Script 00 separa la creación administrativa de la cuenta; los scripts 01–04 se ejecutan con permisos de migración sobre modaexpress. Se preservan los cambios que el usuario haga al archivo privado de conexión.

## Revisión de los cambios parciales de Antigravity (27/10/2026)
Se revisaron git log, status, show --stat HEAD, reflog y diff. En esta copia no existe un commit WIP: HEAD era 7b6f0b9 (Etapa 5), con 11 archivos modificados/no seguidos y la configuración privada aparte. Se hizo respaldo externo antes de integrar.
Se conservan characterEncoding=UTF-8 y contentType con charset. Los modelos parciales Solped/SolpedDetalle se sustituyen por Solped/Detalle compartido con ventas, evitando dos contratos incompatibles; el script de ventas se completa con datos de ejemplo y numeradores coherentes.
Cada JSP e include declara pageEncoding UTF-8; todas las páginas HTML reciben meta charset UTF-8 desde la cabecera compartida o su propio head. Los fragmentos de cuerpo y JSP de redirección no generan etiquetas meta duplicadas fuera de head. Filter fija request/response UTF-8; web.xml refuerza la codificación.
La base y sus 11 tablas ya son utf8mb4_unicode_ci. Se comprobaron textos y bytes de María, algodón, clásico y personalización: están bien almacenados, por lo que no se recargan ni sobrescriben datos válidos. Los scripts declaran utf8mb4 explícitamente y las instrucciones usan --default-character-set=utf8mb4.
Se confirma 3307 sin escucha, ausencia de .local en el proyecto y configuración privada ignorada por Git con URL 3306. Se elimina el helper de instancia aislada que ya no se utiliza.

## Etapa 6
Implementados modelos Detalle/Solped/Venta/Comprobante/GuiaRemision, sus interfaces y repositorios JDBC, VentaServlet y vistas con el mismo dashboard. Ítems dinámicos con JavaScript sencillo y precios resueltos en servidor. Correlativos con bloqueo, una venta por SOLPED, un comprobante y una guía por venta.
Transacción única para cabecera, detalle, descuentos y estados; rollback ante error. IGV 18% con BigDecimal, servicios sin stock, factura con RUC. Edición de venta antes de emitir documentos, anulación con devolución única de stock y bajas con dependencias controladas. Tipos de producto usados en pedidos no se cambian.
Supuestos de CRUD: pedidos se editan pendientes; documentos académicos pueden corregirse/eliminarse en orden inverso (guía, comprobante, venta anulada), sin reciclar números. Las cuentas y datos son ficticios. No se implementa facturación electrónica.
Compilación previa: 42 clases Java y 25 JSP; pruebas de Repository y HTTP completas sobre el servicio 3306 aprobadas. Se repiten comprobaciones finales después de integrar los cambios parciales y corregir UTF-8. Documentación del módulo creada con fragmentos reales; la verificación final y el commit se registran al concluir.


## Verificación final y entrega (27/10/2026)
- Ant de NetBeans: clean dist con Java 17 y Tomcat 9, BUILD SUCCESSFUL; 42 clases compiladas y WAR generado.
- herramientas/verificar.py --bd: PBKDF2, Singleton con conexiones diferentes y pruebas transaccionales de ventas aprobadas en el servicio 3306. Incluye rollback, stock insuficiente, servicios, IGV, RUC, documentos únicos y concurrencia.
- Pruebas HTTP sobre el proyecto original: 25 vistas, login, tres intentos, usuarios inexistentes/inactivos, desbloqueo, roles, CSRF, escape HTML, CRUD de usuarios y catálogos, y flujo completo de ventas aprobados. Los registros temporales se retiraron; pueden quedar saltos en numeradores.
- Navegador: inicio de sesión, dashboard, clientes y nueva SOLPED. Se vieron correctamente Perú, María, algodón, Observación y Guías. Agregar y quitar ítems recalcula el total: 2 polos de S/ 35 = subtotal 70, IGV 12.60, total 82.60. No se guardó este pedido de revisión visual.
- Los cuatro análisis existen, con bloques copiados literalmente del código y análisis crítico. PRUEBAS.md contiene ejecución en NetBeans, comandos fish, orden SQL, cambio de clave y límites de Render.
- Configuración privada en 127.0.0.1:3306, ignorada por Git; sin instancia 3307 ni archivos de esa instancia dentro del proyecto. Dockerfile conserva exactamente su contenido inicial. No se realizó push.
- Esta entrega se registra en el commit local «Etapa 6».


## Etapa 7 — revisión e integración (03/11/2026)
Git seguía en 64a53b2 (Etapa 6). Antigravity modificó Usuario.java y el mapeo de UsuarioRepositoryJDBC.java, y creó sql/05_registro_publico.sql. En MariaDB ya estaban aplicados los roles ADMIN/DUENO/VENDEDOR/CLIENTE y las columnas nombre_negocio, token_recuperacion y token_expira. Se respaldaron estos cambios fuera del repositorio y no se volvió a ejecutar el ALTER.

Se completan RegistroServlet, RecuperarServlet y CatalogoServlet; los tres formularios de registro, recuperar.jsp, catalogo.jsp y los includes públicos. Se agregan los cinco enlaces del login, protección CSRF para formularios públicos, validaciones servidor, escape HTML, hash PBKDF2 y persistencia de nombre_negocio tanto al registrar como al editar. El CRUD ADMIN acepta los cuatro roles y conserva la protección de su propia cuenta.

Decisiones: nombre de tienda es el dato del negocio ya preparado por Antigravity. CLIENTE entra al catálogo; DUENO/VENDEDOR acceden al panel operativo compartido, sin gestión de usuarios. No hay separación de inventarios por negocio. Todos los registros públicos quedan ACTIVO; el parámetro del navegador no puede asignar ADMIN ni cambiar el estado inicial. La recuperación solamente simula el envío, almacena un hash de token y su vencimiento de 15 minutos, sin enviar correo ni cambiar claves.

Verificación: compilación Java 17 de 46 clases y páginas nuevas en Tomcat 9. Pruebas HTTP/BD de los tres registros, duplicados, validación, roles manipulados, hash, CSRF, desactivación, revocación de sesiones, token/caducidad, catálogo activo, búsqueda y XSS aprobadas. Se repitieron login/bloqueo, CRUD de usuarios, catálogos y flujo de ventas con resultado correcto. Navegador: enlaces del login, formulario de tienda y búsqueda «algodón», con tildes y diseño correctos.

El borrador anterior de normalización de usuarios quedó fuera de este proyecto y nunca se aplicó a esta base. Esta etapa sigue el plan nuevo, que mantiene el ENUM rol. No se debe ejecutar el borrador 05_usuarios_normalizados.sql junto a 05_registro_publico.sql.

Se conserva la conexión privada en 3306 y el Dockerfile original. No se hace push. El cierre se registra con un commit local de Etapa 7.

Cierre verificado sobre el proyecto original: Ant `dist` BUILD SUCCESSFUL; `herramientas/verificar.py --bd` aprobó seguridad, Singleton y ventas con concurrencia; `test/PruebasPublicas.py` aprobó la Etapa 7 en Tomcat local. Se verificaron 45 fragmentos reales de los análisis, UTF-8, tablas utf8mb4, configuración privada ignorada y puerto 3307 sin servicio. Los datos temporales fueron retirados.
