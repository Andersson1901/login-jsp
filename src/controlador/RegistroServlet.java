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

    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath()+"/registro_comprador.jsp");
    }

    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        response.setHeader("Cache-Control","no-store");
        if(!FormularioPublico.valido(request)) {
            response.setStatus(403);
            request.setAttribute("error","El formulario venció. Vuelve a abrirlo e intenta nuevamente.");
            request.getRequestDispatcher("/registro_comprador.jsp").forward(request,response);
            return;
        }
        String pagina="/registro_comprador.jsp";
        Usuario u=new Usuario();
        try {
            String tipo=Web.opcion(Web.requerido(request,"tipo",20),"COMPRADOR","TIENDA","VENDEDOR");
            switch(tipo) {
                case "TIENDA": u.setRol("DUENO"); pagina="/registro_tienda.jsp"; break;
                case "VENDEDOR": u.setRol("VENDEDOR"); pagina="/solicitud_vendedor.jsp"; break;
                default: u.setRol("CLIENTE");
            }
            // El rol y estado públicos se fijan aquí; nunca se admite registrar ADMIN.
            u.setEstado("ACTIVO");
            u.setNombres(Web.requerido(request,"nombres",100));
            u.setApellidos(Web.requerido(request,"apellidos",100));
            u.setDni(Web.requerido(request,"dni",8));
            if(!u.getDni().matches("[0-9]{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
            u.setCorreo(Web.requerido(request,"correo",150)); Web.correo(u.getCorreo());
            u.setUsuario(Web.requerido(request,"usuario",50));
            if("DUENO".equals(u.getRol())) u.setNombreNegocio(Web.requerido(request,"nombreNegocio",150));
            u.setClaveHash(Seguridad.generarHash(Web.clave(request)));
            usuarios.guardar(u);
            response.sendRedirect(request.getContextPath()+"/login.jsp?registro=1");
            return;
        } catch(IllegalArgumentException e) {
            response.setStatus(400); request.setAttribute("error",e.getMessage());
        } catch(SQLException e) {
            if(e.getErrorCode()==1062) {
                response.setStatus(409); request.setAttribute("error","El usuario, DNI o correo ya está registrado.");
            } else {
                log("No se pudo registrar una cuenta",e);
                response.setStatus(503); request.setAttribute("error","No se pudo registrar la cuenta. Intenta nuevamente más tarde.");
            }
        }
        u.setClaveHash(null);
        request.setAttribute("registro",u);
        request.getRequestDispatcher(pagina).forward(request,response);
    }
}
