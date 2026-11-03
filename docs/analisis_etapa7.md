<!-- Autor: Rodriguez Alvarado Andersson Eduardo -->
# Etapa 7: registro público y catálogo

Nombre de archivo: RegistroServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import modelo.Usuario;
import repositorio.*;
import util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {
    private final UsuarioRepository usuarios=Repositorios.usuarios();
```
Explicación: El controlador trabaja con UsuarioRepository y queda disponible en /registro.

Bloque 2:
```java
            String tipo=Web.opcion(Web.requerido(request,"tipo",20),"COMPRADOR","TIENDA","VENDEDOR");
            switch(tipo) {
                case "TIENDA": u.setRol("DUENO"); pagina="/registro_tienda.jsp"; break;
                case "VENDEDOR": u.setRol("VENDEDOR"); pagina="/solicitud_vendedor.jsp"; break;
                default: u.setRol("CLIENTE");
            }
            // El rol y estado públicos se fijan aquí; nunca se admite registrar ADMIN.
            u.setEstado("ACTIVO");
```
Explicación: El tipo de formulario fija el rol en el servidor; el estado inicial siempre es ACTIVO.

Bloque 3:
```java
            u.setClaveHash(Seguridad.generarHash(Web.clave(request)));
            usuarios.guardar(u);
            response.sendRedirect(request.getContextPath()+"/login.jsp?registro=1");
            return;
```
Explicación: La contraseña se transforma en PBKDF2 antes de guardar; el alta termina en el login.

Análisis crítico del código fuente: El registro valida y persiste cuentas sin aprobación, como pide la etapa. DUENO y VENDEDOR comparten el panel de esta tienda; no representa una arquitectura de múltiples comercios aislados.

Nombre de archivo: RecuperarServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import repositorio.*;
import util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {
    private final UsuarioRepository usuarios=Repositorios.usuarios();
```
Explicación: Usa el mismo repositorio de usuarios para la recuperación simulada.

Bloque 2:
```java
                String correo=Web.requerido(request,"correo",150); Web.correo(correo);
                usuarios.simularRecuperacion(correo);
                response.sendRedirect(request.getContextPath()+"/recuperar.jsp?enviado=1");
                return;
```
Explicación: Valida el correo y presenta un resultado genérico sin revelar si existe la cuenta.

Análisis crítico del código fuente: No hay proveedor de correo ni endpoint para consumir el token: el alcance es simular el envío. El token queda solo en la BD, caduca a los 15 minutos y no se imprime en HTML o logs.

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
```
Explicación: JDBC concentra SQL y mapeo; los controladores usan la interfaz.

Bloque 2:
```java
    @Override public void simularRecuperacion(String correo) throws SQLException {
        // El token nunca se devuelve al navegador ni se escribe en los registros.
        byte[] aleatorio=new byte[32];
        new java.security.SecureRandom().nextBytes(aleatorio);
        String hash;
        try {
            hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(aleatorio));
        } catch(java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        try(Connection c=ConexionBD.getInstancia().getConexion();
            PreparedStatement p=c.prepareStatement("UPDATE usuarios SET token_recuperacion=?,token_expira=DATE_ADD(NOW(),INTERVAL 15 MINUTE) WHERE correo=? AND estado='ACTIVO'")) {
            p.setString(1,hash); p.setString(2,correo); p.executeUpdate();
        }
    }
```
Explicación: Genera entropía aleatoria, almacena su hash y actualiza solamente cuentas activas con una sentencia parametrizada.

Análisis crítico del código fuente: El correo es único y el UPDATE es atómico; repetir la solicitud reemplaza el token anterior. Para un flujo real habría que entregar el token por un canal verificado y consumirlo una sola vez antes de su vencimiento.

Nombre de archivo: CatalogoServlet.java

Bloque 1:
```java
// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import repositorio.*;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {
    private final ProductoRepository productos=Repositorios.productos();
```
Explicación: El catálogo público depende de ProductoRepository.

Bloque 2:
```java
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        request.setAttribute("catalogoCargado",true);
        try {
            String q=Web.texto(request,"q",100);
            request.setAttribute("q",q);
            request.setAttribute("productos",productos.listarActivos(q));
        } catch(IllegalArgumentException e) {
            response.setStatus(400); request.setAttribute("error",e.getMessage());
        } catch(SQLException e) {
            log("No se pudo cargar el catálogo",e);
            response.setStatus(503); request.setAttribute("error","No se pudo cargar el catálogo. Intenta nuevamente más tarde.");
        }
        request.getRequestDispatcher("/catalogo.jsp").forward(request,response);
    }
}
```
Explicación: Consulta productos activos, limita el texto de búsqueda y entrega la vista; los errores técnicos solo se registran en el servidor.

Análisis crítico del código fuente: El catálogo es de lectura y las vistas escapan el texto. No incorpora carrito, pagos, fotografías o paginación; esos procesos quedan fuera de este plan.
