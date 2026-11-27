// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

// Resultado del reporte de ventas y pedidos de un vendedor o de toda la tienda en un rango de fechas.
public class ReporteVentas {
    public static class Punto {
        private final String etiqueta;
        private final BigDecimal cantidad;
        private final BigDecimal importe;
        public Punto(String etiqueta,BigDecimal cantidad,BigDecimal importe) { this.etiqueta=etiqueta; this.cantidad=cantidad; this.importe=importe; }
        public String getEtiqueta() { return etiqueta; }
        public BigDecimal getCantidad() { return cantidad; }
        public BigDecimal getImporte() { return importe; }
    }
    private String vendedor;
    private LocalDate desde;
    private LocalDate hasta;
    private final Map<String,Integer> pedidosPorEstado=new LinkedHashMap<>();
    private int ventasEmitidas;
    private int ventasAnuladas;
    private BigDecimal ingresos=BigDecimal.ZERO.setScale(2);
    private BigDecimal montoAnulado=BigDecimal.ZERO.setScale(2);
    private final List<Punto> porDia=new ArrayList<>();
    private final List<Punto> productos=new ArrayList<>();
    private final List<Punto> clientes=new ArrayList<>();
    private final List<Object[]> ventas=new ArrayList<>();
    private final List<Object[]> pedidos=new ArrayList<>();
    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
    public Map<String,Integer> getPedidosPorEstado() { return pedidosPorEstado; }
    public int getVentasEmitidas() { return ventasEmitidas; }
    public void setVentasEmitidas(int ventasEmitidas) { this.ventasEmitidas = ventasEmitidas; }
    public int getVentasAnuladas() { return ventasAnuladas; }
    public void setVentasAnuladas(int ventasAnuladas) { this.ventasAnuladas = ventasAnuladas; }
    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }
    public BigDecimal getMontoAnulado() { return montoAnulado; }
    public void setMontoAnulado(BigDecimal montoAnulado) { this.montoAnulado = montoAnulado; }
    public List<Punto> getPorDia() { return porDia; }
    public List<Punto> getProductos() { return productos; }
    public List<Punto> getClientes() { return clientes; }
    public List<Object[]> getVentas() { return ventas; }
    public List<Object[]> getPedidos() { return pedidos; }
    public int getTotalPedidos() { return pedidosPorEstado.values().stream().mapToInt(Integer::intValue).sum(); }
    public BigDecimal getTicketPromedio() {
        if(ventasEmitidas==0) return BigDecimal.ZERO.setScale(2);
        return ingresos.divide(BigDecimal.valueOf(ventasEmitidas),2,RoundingMode.HALF_UP);
    }
}
