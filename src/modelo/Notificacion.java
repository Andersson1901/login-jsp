// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.math.BigDecimal;

public class Notificacion {
    private int idNotificacion;
    private int idUsuario;
    private int idVenta;
    private String nroVenta;
    private String cliente;
    private String estadoAnterior;
    private String estadoNuevo;
    private BigDecimal total;
    private java.sql.Timestamp fecha;
    private boolean leida;
    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public String getNroVenta() { return nroVenta; }
    public void setNroVenta(String nroVenta) { this.nroVenta = nroVenta; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public java.sql.Timestamp getFecha() { return fecha; }
    public void setFecha(java.sql.Timestamp fecha) { this.fecha = fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
