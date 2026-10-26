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
