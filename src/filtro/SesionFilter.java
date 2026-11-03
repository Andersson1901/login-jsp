// Autor: Rodriguez Alvarado Andersson Eduardo
package filtro;

import java.io.IOException;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public class SesionFilter implements Filter {
    private static final Set<String> PUBLICAS = Set.of("/", "/index.jsp", "/login.jsp", "/validar.jsp", "/bloqueado.jsp",
            "/registro_comprador.jsp", "/registro_tienda.jsp", "/solicitud_vendedor.jsp",
            "/recuperar.jsp", "/catalogo.jsp", "/registro", "/recuperar", "/catalogo");

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) peticion;
        HttpServletResponse response = (HttpServletResponse) respuesta;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        String ruta = request.getServletPath();
        response.setHeader("Cache-Control", "no-store");
        if (PUBLICAS.contains(ruta) || ruta.startsWith("/assets/")) {
            cadena.doFilter(request, response);
            return;
        }
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        try {
            repositorio.UsuarioRepository usuarios = repositorio.Repositorios.usuarios();
            modelo.Usuario actual = usuarios.buscarPorId((Integer) sesion.getAttribute("idUsuario"));
            if (actual == null || !"ACTIVO".equals(actual.getEstado())) {
                sesion.invalidate();
                response.sendRedirect(request.getContextPath() + "/bloqueado.jsp");
                return;
            }
            sesion.setAttribute("rol", actual.getRol());
            sesion.setAttribute("usuario", actual.getUsuario());
            sesion.setAttribute("nombreUsuario", actual.getNombres());
        } catch (java.sql.SQLException e) {
            request.getServletContext().log("No se pudo comprobar la sesión", e);
            response.setStatus(503);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("El sistema no está disponible. Intenta nuevamente en unos momentos.");
            return;
        }
        if (sesion.getAttribute("csrf") == null) sesion.setAttribute("csrf", java.util.UUID.randomUUID().toString());
        if ("POST".equals(request.getMethod()) && !sesion.getAttribute("csrf").equals(request.getParameter("csrf"))) {
            response.setStatus(403);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("La solicitud venció. Recarga la página e intenta nuevamente.");
            return;
        }
        // Los compradores usan el catálogo público; no acceden a los datos internos.
        String rol=(String)sesion.getAttribute("rol");
        if (!Set.of("ADMIN","DUENO","VENDEDOR").contains(rol) && !ruta.equals("/logout.jsp")) {
            if ("GET".equals(request.getMethod()) && ruta.equals("/bienvenido.jsp")) {
                response.sendRedirect(request.getContextPath()+"/catalogo");
            } else {
                response.setStatus(403);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Tu cuenta permite consultar el catálogo de la tienda.");
            }
            return;
        }
        if (ruta.equals("/usuarios") && !"ADMIN".equals(sesion.getAttribute("rol"))) {
            response.setStatus(403);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("No tienes permiso para administrar usuarios.");
            return;
        }
        cadena.doFilter(request, response);
    }
}
