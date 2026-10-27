// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.GuiaRemision;
import java.sql.SQLException;
public interface GuiaRepository extends Repository<GuiaRemision, Integer> {
    GuiaRemision buscarPorVenta(Integer idVenta) throws SQLException;
}
