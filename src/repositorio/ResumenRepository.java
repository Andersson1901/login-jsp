// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import java.sql.SQLException;
import java.util.Map;
public interface ResumenRepository {
    Map<String, Number> obtener() throws SQLException;
}
