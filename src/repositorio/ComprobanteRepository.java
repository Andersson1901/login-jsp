// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Comprobante;
import java.sql.SQLException;
public interface ComprobanteRepository extends Repository<Comprobante, Integer> {
    Comprobante buscarPorVenta(Integer idVenta) throws SQLException;
    Pagina<Comprobante> buscar(Consulta consulta) throws SQLException;
}
