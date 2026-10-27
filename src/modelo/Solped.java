// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.math.BigDecimal;
import java.util.*;

public class Solped {
    private int idSolped;
    private String nroSolped;
    private int idCliente;
    private String cliente;
    private java.sql.Timestamp fecha;
    private String estado;
    private String observacion;
    private int idUsuario;
    private List<Detalle> detalles = new ArrayList<>();
    public int getIdSolped() { return idSolped; }
    public void setIdSolped(int idSolped) { this.idSolped = idSolped; }
    public String getNroSolped() { return nroSolped; }
    public void setNroSolped(String nroSolped) { this.nroSolped = nroSolped; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public java.sql.Timestamp getFecha() { return fecha; }
    public void setFecha(java.sql.Timestamp fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public List<Detalle> getDetalles() { return detalles; }
    public void setDetalles(List<Detalle> detalles) { this.detalles = detalles; }
    public BigDecimal getTotal() { return detalles.stream().map(Detalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
