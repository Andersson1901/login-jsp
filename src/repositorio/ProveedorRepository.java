// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Proveedor;
import java.sql.SQLException;
import java.util.List;

public interface ProveedorRepository extends Repository<Proveedor, Integer> {
    List<Proveedor> listar(String busqueda) throws SQLException;
}
