// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Proveedor;
import repositorio.ProveedorRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/proveedores")
public class ProveedorServlet extends HttpServlet {
    private final ProveedorRepository repositorio = Repositorios.proveedores();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.texto(request,"accion",20);
            if("nuevo".equals(accion)||"editar".equals(accion)) {
                Proveedor entidad="nuevo".equals(accion)?new Proveedor():repositorio.buscarPorId(Web.id(request,"id"));
                if(entidad==null) throw new IllegalArgumentException("El registro no existe.");
                request.setAttribute("registro",entidad);
                request.getRequestDispatcher("/WEB-INF/vistas/proveedores_formulario.jsp").forward(request,response);
            } else {
                String q=Web.texto(request,"q",100); request.setAttribute("q",q);
                request.setAttribute("registros",repositorio.listar(q));
                request.getRequestDispatcher("/WEB-INF/vistas/proveedores.jsp").forward(request,response);
            }
        } catch(SQLException e) {
            log("Error al consultar proveedores",e); request.setAttribute("error","No se pudo cargar la información.");
            request.getRequestDispatcher("/WEB-INF/vistas/proveedores.jsp").forward(request,response);
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+"/proveedores"); }
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.requerido(request,"accion",20);
            if("eliminar".equals(accion)) repositorio.eliminar(Web.id(request,"id"));
            else if("guardar".equals(accion)) {
                Proveedor entidad=new Proveedor();
                String id=Web.texto(request,"id",10); entidad.setIdProveedor(id.isEmpty()?0:Web.id(request,"id"));
                entidad.setRuc(Web.requerido(request,"ruc",11));
                entidad.setRazonSocial(Web.requerido(request,"razonSocial",150));
                entidad.setContacto(Web.requerido(request,"contacto",100));
                entidad.setTelefono(Web.requerido(request,"telefono",20));
                entidad.setCorreo(Web.requerido(request,"correo",150));
                entidad.setDireccion(Web.requerido(request,"direccion",200));
                entidad.setEstado(Web.opcion(Web.requerido(request,"estado",20),"ACTIVO","INACTIVO"));
                if(!entidad.getRuc().matches("[0-9]{11}")) throw new IllegalArgumentException("El RUC debe tener 11 dígitos.");
                Web.correo(entidad.getCorreo());
                if(entidad.getIdProveedor()==0) repositorio.guardar(entidad); else repositorio.actualizar(entidad);
            } else throw new IllegalArgumentException("Acción no válida.");
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) { log("Error al modificar proveedores",e); Web.mensaje(request,e.getErrorCode()==1062?"El identificador único ya está registrado.":"No se pudo guardar o eliminar. Si el registro tiene movimientos, cámbialo a INACTIVO."); }
        response.sendRedirect(request.getContextPath()+"/proveedores");
    }
}
