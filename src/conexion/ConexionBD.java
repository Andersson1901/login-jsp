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

    public ConexionBD() {
        configuracion.setProperty("url", "jdbc:mariadb://127.0.0.1:3307/modaexpress");
        configuracion.setProperty("usuario", "moda_dev");
        configuracion.setProperty("clave", "ModaPrueba2026");
        try (InputStream archivo = ConexionBD.class.getResourceAsStream("/conexion.properties")) {
            if (archivo != null) configuracion.load(archivo);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración de conexión", e);
        }
    }

    public Connection getConexion() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Falta el driver JDBC de MariaDB", e);
        }
        return DriverManager.getConnection(configuracion.getProperty("url"),
                configuracion.getProperty("usuario"), configuracion.getProperty("clave"));
    }

    public static void main(String[] args) {
        try (Connection conexion = new ConexionBD().getConexion()) {
            System.out.println("Conexión correcta a " + conexion.getCatalog());
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.exit(1);
        }
    }
}
