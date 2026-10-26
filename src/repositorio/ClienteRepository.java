// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface ClienteRepository extends Repository<Cliente, Integer> {
    List<Cliente> listar(String busqueda) throws SQLException;
}
