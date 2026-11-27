// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Solped;
import java.sql.SQLException;
public interface SolpedRepository extends Repository<Solped, Integer> {
    void cambiarEstado(Integer id, String estado) throws SQLException;
    Pagina<Solped> buscar(Consulta consulta) throws SQLException;
}
