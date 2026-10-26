// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T, ID> {
    List<T> listar() throws SQLException;
    T buscarPorId(ID id) throws SQLException;
    void guardar(T entidad) throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(ID id) throws SQLException;
}
