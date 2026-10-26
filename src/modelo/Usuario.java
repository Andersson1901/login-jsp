// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;

public class Usuario {
    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String dni;
    private String correo;
    private String usuario;
    private String claveHash;
    private String rol;
    private String estado;
    private int intentosFallidos;
    private java.sql.Timestamp fechaCreacion;
    private java.sql.Timestamp ultimoAcceso;

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public java.sql.Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public java.sql.Timestamp getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(java.sql.Timestamp ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
