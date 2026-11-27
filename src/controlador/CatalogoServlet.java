// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import modelo.Consulta;
import repositorio.*;
import util.Paginacion;
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
            Consulta consulta=Paginacion.consulta(request,"");
            request.setAttribute("q",consulta.getBusqueda());
            request.setAttribute("productos",Paginacion.listar(request,consulta,productos::buscarActivos).getFilas());
        } catch(IllegalArgumentException e) {
            response.setStatus(400); request.setAttribute("error",e.getMessage());
        } catch(SQLException e) {
            log("No se pudo cargar el catálogo",e);
            response.setStatus(503); request.setAttribute("error","No se pudo cargar el catálogo. Intenta nuevamente más tarde.");
        }
        request.getRequestDispatcher("/catalogo.jsp").forward(request,response);
    }
}
