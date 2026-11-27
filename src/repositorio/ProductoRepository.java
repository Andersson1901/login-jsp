// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Producto;
import java.sql.SQLException;
import java.util.List;

public interface ProductoRepository extends Repository<Producto, Integer> {
    List<Producto> listarActivos(String busqueda) throws SQLException;
    List<Producto> listar(String busqueda) throws SQLException;
    Pagina<Producto> buscar(Consulta consulta) throws SQLException;
    Pagina<Producto> buscarActivos(Consulta consulta) throws SQLException;
}
