// Autor: Rodriguez Alvarado Andersson Eduardo
package modelo;

public class ResultadoAcceso {
    private final Usuario usuario;
    private final boolean bloqueado;
    public ResultadoAcceso(Usuario usuario, boolean bloqueado) {
        this.usuario = usuario;
        this.bloqueado = bloqueado;
    }
    public Usuario getUsuario() { return usuario; }
    public boolean isBloqueado() { return bloqueado; }
}
