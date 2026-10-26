// Autor: Rodriguez Alvarado Andersson Eduardo
package filtro;

import java.io.IOException;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public class SesionFilter implements Filter {
    private static final Set<String> PUBLICAS = Set.of("/", "/index.jsp", "/login.jsp", "/validar.jsp", "/bloqueado.jsp");

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) peticion;
        HttpServletResponse response = (HttpServletResponse) respuesta;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        String ruta = request.getServletPath();
        if (PUBLICAS.contains(ruta) || ruta.startsWith("/assets/")) {
            cadena.doFilter(request, response);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        cadena.doFilter(request, response);
    }
}
