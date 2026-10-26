<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Repository de usuarios

Nombre de archivo: Repository.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T, ID> {
    List<T> listar() throws SQLException;
    T buscarPorId(ID id) throws SQLException;
    void guardar(T entidad) throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(ID id) throws SQLException;
}
```
Explicación: Define las cinco operaciones comunes con entidad e identificador parametrizados.

Análisis crítico del código fuente: Unifica el contrato sin obligar a los controladores a conocer SQL. SQLException aún acopla el contrato a JDBC; para una tarea pequeña evita una jerarquía adicional de excepciones.

Nombre de archivo: UsuarioRepository.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import modelo.Usuario;
import modelo.ResultadoAcceso;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioRepository extends Repository<Usuario, Integer> {
    List<Usuario> listar(String busqueda) throws SQLException;
    Usuario buscarPorUsuario(String usuario) throws SQLException;
    void desbloquear(Integer id) throws SQLException;
    void cambiarClave(Integer id, String hash) throws SQLException;
    ResultadoAcceso autenticar(String nombre, String clave) throws SQLException;
}
```
Explicación: Extiende el CRUD con búsqueda, desbloqueo, cambio de clave y autenticación.

Análisis crítico del código fuente: La interfaz mantiene juntas las operaciones de usuarios. Autenticación también contiene reglas del negocio; una aplicación más grande las separaría en un servicio.

Nombre de archivo: UsuarioRepositoryJDBC.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Usuario;
import modelo.ResultadoAcceso;
import util.Seguridad;
import java.sql.*;
import java.util.*;

public class UsuarioRepositoryJDBC implements UsuarioRepository {
    @Override public List<Usuario> listar() throws SQLException { return listar(""); }
```
Explicación: La implementación selecciona JDBC y usa la conexión configurada.

Bloque 2:
```java
public List<Usuario> listar(String busqueda) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = ConexionBD.getInstancia().getConexion();
             PreparedStatement p = c.prepareStatement("SELECT * FROM usuarios WHERE usuario LIKE ? OR nombres LIKE ? OR apellidos LIKE ? OR dni LIKE ? ORDER BY id_usuario")) {
            for (int i=1; i<=4; i++) p.setString(i, "%" + busqueda + "%");
            try (ResultSet r=p.executeQuery()) { while (r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
```
Explicación: La búsqueda usa parámetros y cierra conexión, consulta y resultados.

Bloque 3:
```java
int intentos = usuario.getIntentosFallidos() + 1;
                try (PreparedStatement cambio = conexion.prepareStatement("UPDATE usuarios SET intentos_fallidos=?, estado=? WHERE id_usuario=?")) {
                    cambio.setInt(1, intentos);
                    cambio.setString(2, intentos >= 3 ? "BLOQUEADO" : "ACTIVO");
                    cambio.setInt(3, usuario.getIdUsuario());
                    cambio.executeUpdate();
                }
                conexion.commit();
                return new ResultadoAcceso(null, intentos >= 3);
```
Explicación: El incremento y bloqueo forman parte de la transacción con SELECT FOR UPDATE.

Análisis crítico del código fuente: Repository concentra SQL y mapeo. La consulta completa carga todos los resultados y no hay paginación; las conexiones nuevas tienen costo y un pool sería recomendable en producción.

Nombre de archivo: UsuarioServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import repositorio.UsuarioRepository;
import repositorio.Repositorios;
import modelo.Usuario;
import util.Seguridad;
import util.Web;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {
    private final UsuarioRepository usuarios = Repositorios.usuarios();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
```
Explicación: El controlador depende de UsuarioRepository; la fábrica decide la implementación.

Bloque 2:
```java
if(u.getIdUsuario()==0) { u.setClaveHash(Seguridad.generarHash(Web.clave(request))); usuarios.guardar(u); }
                else {
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario") && (!"ACTIVO".equals(u.getEstado()) || !"ADMIN".equals(u.getRol())))
                        throw new IllegalArgumentException("Conserva tu cuenta activa con rol ADMIN para no perder el acceso.");
                    usuarios.actualizar(u);
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario")) {
                        request.getSession().setAttribute("usuario",u.getUsuario()); request.getSession().setAttribute("nombreUsuario",u.getNombres());
                    }
                }
```
Explicación: El servlet valida la petición y delega la persistencia al contrato.

Análisis crítico del código fuente: La separación permite cambiar la persistencia sin cambiar las vistas. El servlet conserva validaciones y sesión HTTP; para pruebas unitarias más aisladas convendría inyectar el repositorio por constructor.
