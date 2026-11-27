// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import modelo.Consulta;
import modelo.Pagina;
import modelo.ReporteVentas;
import java.sql.SQLException;
import java.time.LocalDate;

public interface ReporteRepository {
    // idVendedor null: toda la tienda. Las fechas son el rango inclusivo del reporte.
    ReporteVentas ventas(Integer idVendedor, LocalDate desde, LocalDate hasta) throws SQLException;
    // Detalle de ventas y de pedidos del mismo rango, paginado y ordenable (el Excel sigue trayendo hasta 5000 filas).
    Pagina<Object[]> ventasDetalle(Integer idVendedor, LocalDate desde, LocalDate hasta, Consulta consulta) throws SQLException;
    Pagina<Object[]> pedidosDetalle(Integer idVendedor, LocalDate desde, LocalDate hasta, Consulta consulta) throws SQLException;
}
