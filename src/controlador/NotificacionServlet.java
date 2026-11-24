// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import repositorio.NotificacionRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// El filtro limita esta ruta al rol DUENO; cada dueño solo ve sus propios avisos.
@WebServlet("/notificaciones")
public class NotificacionServlet extends HttpServlet {
    private final NotificacionRepository repositorio = Repositorios.notificaciones();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try { request.setAttribute("registros",repositorio.listarPorUsuario(usuario(request))); }
        catch(SQLException e) { log("Error al consultar notificaciones",e); request.setAttribute("error","No se pudo cargar la información."); }
        request.getRequestDispatcher("/WEB-INF/vistas/notificaciones.jsp").forward(request,response);
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            if(!"leer".equals(Web.requerido(request,"accion",20))) throw new IllegalArgumentException("Acción no válida.");
            repositorio.marcarLeidas(usuario(request));
            Web.mensaje(request,"Notificaciones marcadas como leídas.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) { log("Error al actualizar notificaciones",e); Web.mensaje(request,"No se pudo completar la operación. Intenta nuevamente."); }
        response.sendRedirect(request.getContextPath()+"/notificaciones");
    }
    private int usuario(HttpServletRequest request) { return (Integer)request.getSession().getAttribute("idUsuario"); }
}
