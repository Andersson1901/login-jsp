// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import repositorio.UsuarioRepository;
import repositorio.Repositorios;
import modelo.Usuario;
import util.Seguridad;
import util.Web;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {
    private final UsuarioRepository usuarios = Repositorios.usuarios();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String accion=Web.texto(request,"accion",20);
            if ("nuevo".equals(accion) || "editar".equals(accion) || "clave".equals(accion)) {
                Usuario u="nuevo".equals(accion) ? new Usuario() : usuarios.buscarPorId(Web.id(request,"id"));
                if(u==null) throw new IllegalArgumentException("El usuario no existe.");
                if(u.getIdUsuario()==0) { u.setRol("VENDEDOR"); u.setEstado("ACTIVO"); }
                request.setAttribute("registro",u);
                request.getRequestDispatcher("/WEB-INF/vistas/"+("clave".equals(accion)?"usuario_clave.jsp":"usuario_formulario.jsp")).forward(request,response);
            } else {
                String q=Web.texto(request,"q",100);
                request.setAttribute("q",q); request.setAttribute("registros",usuarios.listar(q));
                request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request,response);
            }
        } catch (SQLException e) {
            log("Error al consultar usuarios",e); request.setAttribute("error","No se pudo cargar la información. Intenta nuevamente.");
            request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request,response);
        } catch (IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+"/usuarios"); }
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String accion=Web.requerido(request,"accion",20);
            if ("guardar".equals(accion)) {
                Usuario u = new Usuario();
                String id=Web.texto(request,"id",10);
                u.setIdUsuario(id.isEmpty()?0:Web.id(request,"id"));
                u.setNombres(Web.requerido(request,"nombres",100)); u.setApellidos(Web.requerido(request,"apellidos",100));
                u.setDni(Web.requerido(request,"dni",8)); if(!u.getDni().matches("[0-9]{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
                u.setCorreo(Web.requerido(request,"correo",150)); Web.correo(u.getCorreo());
                u.setUsuario(Web.requerido(request,"usuario",50));
                u.setRol(Web.opcion(Web.requerido(request,"rol",10),"ADMIN","DUENO","VENDEDOR","CLIENTE"));
                u.setNombreNegocio("DUENO".equals(u.getRol())?Web.requerido(request,"nombreNegocio",150):null);
                u.setEstado(Web.opcion(Web.requerido(request,"estado",10),"ACTIVO","BLOQUEADO","INACTIVO"));
                if(u.getIdUsuario()==0) { u.setClaveHash(Seguridad.generarHash(Web.clave(request))); usuarios.guardar(u); }
                else {
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario") && (!"ACTIVO".equals(u.getEstado()) || !"ADMIN".equals(u.getRol())))
                        throw new IllegalArgumentException("Conserva tu cuenta activa con rol ADMIN para no perder el acceso.");
                    usuarios.actualizar(u);
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario")) {
                        request.getSession().setAttribute("usuario",u.getUsuario()); request.getSession().setAttribute("nombreUsuario",u.getNombres());
                    }
                }
            } else {
                int id=Web.id(request,"id");
                if(usuarios.buscarPorId(id)==null) throw new IllegalArgumentException("El usuario no existe.");
                switch(accion) {
                    case "eliminar":
                        if(id==(Integer)request.getSession().getAttribute("idUsuario")) throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
                        usuarios.eliminar(id); break;
                    case "desbloquear": usuarios.desbloquear(id); break;
                    case "clave": usuarios.cambiarClave(id,Seguridad.generarHash(Web.clave(request))); break;
                    default: throw new IllegalArgumentException("Acción no válida.");
                }
            }
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch (IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch (SQLException e) {
            log("Error al modificar usuario",e);
            Web.mensaje(request,e.getErrorCode()==1062?"El usuario, DNI o correo ya está registrado.":"No se pudo guardar: el registro puede estar relacionado con otros datos.");
        }
        response.sendRedirect(request.getContextPath()+"/usuarios");
    }
}
