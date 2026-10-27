<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Análisis Singleton

Nombre de archivo: ConexionBD.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private final Properties configuracion = new Properties();

    private ConexionBD() {
```
Explicación: La clase concentra los parámetros de conexión. El constructor privado impide crear instancias desde las peticiones.

Bloque 2:
```java
    private static class Contenedor {
        private static final ConexionBD INSTANCIA = new ConexionBD();
    }

    public static ConexionBD getInstancia() {
        return Contenedor.INSTANCIA;
    }


```
Explicación: La inicialización de clases de Java publica la instancia de forma segura entre hilos, sin bloquear cada consulta.

Bloque 3:
```java
public Connection getConexion() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Falta el driver JDBC de MariaDB", e);
        }
        Connection conexion = DriverManager.getConnection(configuracion.getProperty("url"),
                configuracion.getProperty("usuario"), configuracion.getProperty("clave"));
        try (java.sql.PreparedStatement consulta = conexion.prepareStatement("SET time_zone = ?")) {
            consulta.setString(1, "-05:00");
            consulta.execute();
            return conexion;
        } catch (SQLException e) {
            conexion.close();
            throw e;
        }
    }
```
Explicación: Cada llamada obtiene una conexión nueva. Quien la solicita la cierra con try-with-resources.

Análisis crítico del código fuente: Singleton evita repetir la carga de configuración dentro de una aplicación desplegada, pero no crea un pool ni una instancia global entre varios servidores. Compartir una Connection sería inseguro: una petición podría cerrar o confirmar la transacción de otra. La configuración solo se recarga al reiniciar la aplicación.
