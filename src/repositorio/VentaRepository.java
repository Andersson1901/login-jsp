// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Venta;
import java.sql.SQLException;
public interface VentaRepository extends Repository<Venta, Integer> {
    int crearDesdeSolped(Integer idSolped, Integer idUsuario) throws SQLException;
    void anular(Integer id) throws SQLException;
    Pagina<Venta> buscar(Consulta consulta) throws SQLException;
}
