// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.GuiaRemision;
import java.sql.SQLException;
public interface GuiaRepository extends Repository<GuiaRemision, Integer> {
    GuiaRemision buscarPorVenta(Integer idVenta) throws SQLException;
    Pagina<GuiaRemision> buscar(Consulta consulta) throws SQLException;
}
