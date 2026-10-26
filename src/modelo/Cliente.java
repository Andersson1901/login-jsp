// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;

import java.math.BigDecimal;

public class Cliente {
    private int idCliente;
    private String tipoDocumento;
    private String nroDocumento;
    private String nombresRazonSocial;
    private String direccion;
    private String telefono;
    private String correo;
    private String estado;
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }
    public String getNombresRazonSocial() { return nombresRazonSocial; }
    public void setNombresRazonSocial(String nombresRazonSocial) { this.nombresRazonSocial = nombresRazonSocial; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
