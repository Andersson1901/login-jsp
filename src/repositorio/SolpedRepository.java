// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Solped;
import java.sql.SQLException;
public interface SolpedRepository extends Repository<Solped, Integer> {
    void cambiarEstado(Integer id, String estado) throws SQLException;
}
