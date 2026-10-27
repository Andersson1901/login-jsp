// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Comprobante;
import java.sql.SQLException;
public interface ComprobanteRepository extends Repository<Comprobante, Integer> {
    Comprobante buscarPorVenta(Integer idVenta) throws SQLException;
}
