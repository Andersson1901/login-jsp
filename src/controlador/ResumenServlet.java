// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import repositorio.ResumenRepository;
import repositorio.Repositorios;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/resumen")
public class ResumenServlet extends HttpServlet {
    private final ResumenRepository repositorio = Repositorios.resumen();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try { request.setAttribute("resumen",repositorio.obtener()); }
        catch(SQLException e) { log("No se pudo cargar el resumen",e); request.setAttribute("errorResumen",true); }
        request.getRequestDispatcher("/WEB-INF/vistas/resumen.jsp").include(request,response);
    }
}
