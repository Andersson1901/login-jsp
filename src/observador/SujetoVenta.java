// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sujeto: único por classloader, como ConexionBD. Los observadores se suscriben y reciben cada cambio de estado.
public class SujetoVenta {
    private static final Logger REGISTRO=Logger.getLogger(SujetoVenta.class.getName());
    private final CopyOnWriteArrayList<ObservadorVenta> observadores=new CopyOnWriteArrayList<>();

    private SujetoVenta() { }

    private static class Contenedor {
        private static final SujetoVenta INSTANCIA=new SujetoVenta();
    }

    public static SujetoVenta getInstancia() { return Contenedor.INSTANCIA; }
    public void suscribir(ObservadorVenta observador) { observadores.addIfAbsent(observador); }
    public void cancelar(ObservadorVenta observador) { observadores.remove(observador); }

    // La venta ya está confirmada: el fallo de un observador se registra y no afecta a la venta ni a los demás.
    public void notificar(EventoVenta evento) {
        for(ObservadorVenta observador:observadores) {
            try { observador.actualizar(evento); }
            catch(SQLException | RuntimeException e) { REGISTRO.log(Level.WARNING,"Un observador de ventas falló",e); }
        }
    }
}
