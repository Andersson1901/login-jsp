// Autor: Rodriguez Alvarado Andersson Eduardo
package observador;
import java.sql.SQLException;

// Observador: recibe cada cambio de estado de una venta, ya confirmado en la base.
public interface ObservadorVenta {
    void actualizar(EventoVenta evento) throws SQLException;
}
