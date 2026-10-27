// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;
import java.math.BigDecimal;
import java.util.*;

public class GuiaRemision {
    private int idGuia;
    private String serie;
    private int numero;
    private int idVenta;
    private String nroVenta;
    private java.sql.Date fechaTraslado;
    private String puntoPartida;
    private String puntoLlegada;
    private String estado;
    public int getIdGuia() { return idGuia; }
    public void setIdGuia(int idGuia) { this.idGuia = idGuia; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public String getNroVenta() { return nroVenta; }
    public void setNroVenta(String nroVenta) { this.nroVenta = nroVenta; }
    public java.sql.Date getFechaTraslado() { return fechaTraslado; }
    public void setFechaTraslado(java.sql.Date fechaTraslado) { this.fechaTraslado = fechaTraslado; }
    public String getPuntoPartida() { return puntoPartida; }
    public void setPuntoPartida(String puntoPartida) { this.puntoPartida = puntoPartida; }
    public String getPuntoLlegada() { return puntoLlegada; }
    public void setPuntoLlegada(String puntoLlegada) { this.puntoLlegada = puntoLlegada; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
