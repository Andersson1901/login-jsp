// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

// Único punto de construcción; los controladores trabajan con las interfaces.
public final class Repositorios {
    private Repositorios() { }
    public static UsuarioRepository usuarios() { return new UsuarioRepositoryJDBC(); }
    public static ClienteRepository clientes() { return new ClienteRepositoryJDBC(); }
    public static ProveedorRepository proveedores() { return new ProveedorRepositoryJDBC(); }
    public static ProductoRepository productos() { return new ProductoRepositoryJDBC(); }
    public static ResumenRepository resumen() { return new ResumenRepositoryJDBC(); }
}
