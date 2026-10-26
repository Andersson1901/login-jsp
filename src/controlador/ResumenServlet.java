// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import conexion.ConexionBD;
import java.io.IOException;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/resumen")
public class ResumenServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection c = ConexionBD.getInstancia().getConexion();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM usuarios"); ResultSet r = p.executeQuery()) {
            r.next(); request.setAttribute("usuariosTotal", r.getInt(1));
        } catch (SQLException e) { log("No se pudo cargar el resumen", e); request.setAttribute("errorResumen", true); }
        request.getRequestDispatcher("/WEB-INF/vistas/resumen.jsp").include(request, response);
    }
}
