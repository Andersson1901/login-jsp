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
