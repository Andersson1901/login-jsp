// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import repositorio.NotificacionRepository;
import java.sql.SQLException;

// Observador concreto: deja un aviso a cada dueño de tienda activo cuando cambia el estado de una venta.
public class NotificadorDueno implements ObservadorVenta {
    private final NotificacionRepository notificaciones;
    public NotificadorDueno(NotificacionRepository notificaciones) { this.notificaciones=notificaciones; }
    @Override public void actualizar(EventoVenta evento) throws SQLException {
        notificaciones.notificarDuenos(evento.getVenta(),evento.getEstadoAnterior(),evento.getEstadoNuevo());
    }
}
