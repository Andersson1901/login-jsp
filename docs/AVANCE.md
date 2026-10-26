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

## Etapa 2 (en verificación)
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
