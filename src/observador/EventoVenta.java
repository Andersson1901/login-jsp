// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import modelo.Venta;

// Cambio de estado de una venta. Al emitirla, el estado anterior es null.
public final class EventoVenta {
    private final Venta venta;
    private final String estadoAnterior;
    private final String estadoNuevo;
    public EventoVenta(Venta venta,String estadoAnterior,String estadoNuevo) {
        this.venta=venta; this.estadoAnterior=estadoAnterior; this.estadoNuevo=estadoNuevo;
    }
    public Venta getVenta() { return venta; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
}
