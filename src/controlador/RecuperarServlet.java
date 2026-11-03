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
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath()+"/recuperar.jsp");
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        response.setHeader("Cache-Control","no-store");
        if(!FormularioPublico.valido(request)) {
            response.setStatus(403); request.setAttribute("error","El formulario venció. Recarga la página.");
        } else {
            try {
                String correo=Web.requerido(request,"correo",150); Web.correo(correo);
                usuarios.simularRecuperacion(correo);
                response.sendRedirect(request.getContextPath()+"/recuperar.jsp?enviado=1");
                return;
            } catch(IllegalArgumentException e) {
                response.setStatus(400); request.setAttribute("error",e.getMessage());
            } catch(SQLException e) {
                log("No se pudo simular la recuperación",e);
                response.setStatus(503); request.setAttribute("error","El servicio no está disponible. Intenta nuevamente más tarde.");
            }
        }
        request.getRequestDispatcher("/recuperar.jsp").forward(request,response);
    }
}
