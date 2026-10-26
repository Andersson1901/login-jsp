// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import java.sql.*;
import java.util.*;
public class ResumenRepositoryJDBC implements ResumenRepository {
    public Map<String, Number> obtener() throws SQLException {
        Map<String, Number> resumen=new LinkedHashMap<>();
        String sql="SELECT (SELECT COUNT(*) FROM usuarios) usuarios, (SELECT COUNT(*) FROM clientes) clientes, (SELECT COUNT(*) FROM proveedores) proveedores, (SELECT COUNT(*) FROM productos) productos";
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            if(r.next()) { for(String campo:List.of("usuarios","clientes","proveedores","productos")) resumen.put(campo,r.getInt(campo)); }
        }
        return resumen;
    }
}
