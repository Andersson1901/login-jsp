// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Notificacion;
import modelo.Venta;
import java.sql.SQLException;
import java.util.List;

public interface NotificacionRepository {
    void notificarDuenos(Venta venta, String estadoAnterior, String estadoNuevo) throws SQLException;
    List<Notificacion> listarPorUsuario(int idUsuario) throws SQLException;
    Pagina<Notificacion> buscar(int idUsuario, Consulta consulta) throws SQLException;
    int contarNoLeidas(int idUsuario) throws SQLException;
    void marcarLeidas(int idUsuario) throws SQLException;
}
