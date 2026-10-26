<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Ejecución local

Proyecto Ant de NetBeans. Java 17, Tomcat 9 (javax.servlet).

## Base de datos aislada
Desde la carpeta del proyecto, comandos compatibles con fish:

```fish
python3 herramientas/bd_local.py iniciar
python3 herramientas/bd_local.py sql sql/01_usuarios_inicial.sql
```

El script 01 se ejecuta una sola vez. La instancia guarda datos en .local/mariadb y escucha solamente en 127.0.0.1:3307; no altera el servicio MariaDB existente.
Para detenerla: `python3 herramientas/bd_local.py detener`.

Cuenta JDBC de prueba: moda_dev / ModaPrueba2026; solo permisos SELECT, INSERT, UPDATE y DELETE en modaexpress. No son secretos reales.
Configuración en src/conexion.properties (ignorada por Git), ejemplo en src/conexion.properties.example. NetBeans copia ese recurso al classpath.
Si falta el archivo, ConexionBD usa los valores locales anteriores. Para usar tu servicio habitual, cambia la URL a puerto 3306 y crea una cuenta con los mismos permisos en ese servidor.
Para cambiar la clave: en una consola administrativa de MariaDB ejecuta `ALTER USER 'moda_dev'@'127.0.0.1' IDENTIFIED BY 'TU_NUEVA_CLAVE';`, actualiza src/conexion.properties y vuelve a compilar y desplegar. No guardes claves reales en scripts ni en Git.

## NetBeans
1. Abrir ~/NetBeansProjects/LoginJSP.
2. Propiedades: plataforma Java 17 y servidor Apache Tomcat 9.
3. Limpiar y construir; ejecutar. URL: http://localhost:8080/LoginJSP/.
4. Etapa 2: usuario admin, clave 1234.

## Render (pendiente deliberadamente)
El Dockerfile original solo copia web/. Para publicar habría que compilar src/ con Java 17 y las bibliotecas de Tomcat, copiar las clases a WEB-INF/classes, incluir los JAR de WEB-INF/lib y configurar una MariaDB accesible desde el servidor con TLS y credenciales privadas. No se cambió Dockerfile ni se ejecutó git push.
