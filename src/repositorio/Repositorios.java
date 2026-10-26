// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

// Único punto de construcción; los controladores trabajan con las interfaces.
public final class Repositorios {
    private Repositorios() { }
    public static UsuarioRepository usuarios() { return new UsuarioRepositoryJDBC(); }
}
