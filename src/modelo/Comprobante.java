// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.math.BigDecimal;
import java.util.*;

public class Comprobante {
    private int idComprobante;
    private String tipo;
    private String serie;
    private int numero;
    private int idVenta;
    private String nroVenta;
    private java.sql.Timestamp fechaEmision;
    private BigDecimal total;
    public int getIdComprobante() { return idComprobante; }
    public void setIdComprobante(int idComprobante) { this.idComprobante = idComprobante; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public String getNroVenta() { return nroVenta; }
    public void setNroVenta(String nroVenta) { this.nroVenta = nroVenta; }
    public java.sql.Timestamp getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(java.sql.Timestamp fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
