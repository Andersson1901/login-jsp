// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import repositorio.Repositorios;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// Suscribe los observadores una sola vez, al iniciar la aplicación.
@WebListener
public class SuscripcionVentas implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent evento) {
        SujetoVenta.getInstancia().suscribir(new NotificadorDueno(Repositorios.notificaciones()));
    }
}
