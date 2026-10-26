// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Cliente;
import repositorio.ClienteRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/clientes")
public class ClienteServlet extends HttpServlet {
    private final ClienteRepository repositorio = Repositorios.clientes();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.texto(request,"accion",20);
            if("nuevo".equals(accion)||"editar".equals(accion)) {
                Cliente entidad="nuevo".equals(accion)?new Cliente():repositorio.buscarPorId(Web.id(request,"id"));
                if(entidad==null) throw new IllegalArgumentException("El registro no existe.");
                request.setAttribute("registro",entidad);
                request.getRequestDispatcher("/WEB-INF/vistas/clientes_formulario.jsp").forward(request,response);
            } else {
                String q=Web.texto(request,"q",100); request.setAttribute("q",q);
                request.setAttribute("registros",repositorio.listar(q));
                request.getRequestDispatcher("/WEB-INF/vistas/clientes.jsp").forward(request,response);
            }
        } catch(SQLException e) {
            log("Error al consultar clientes",e); request.setAttribute("error","No se pudo cargar la información.");
            request.getRequestDispatcher("/WEB-INF/vistas/clientes.jsp").forward(request,response);
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+"/clientes"); }
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.requerido(request,"accion",20);
            if("eliminar".equals(accion)) repositorio.eliminar(Web.id(request,"id"));
            else if("guardar".equals(accion)) {
                Cliente entidad=new Cliente();
                String id=Web.texto(request,"id",10); entidad.setIdCliente(id.isEmpty()?0:Web.id(request,"id"));
                entidad.setTipoDocumento(Web.opcion(Web.requerido(request,"tipoDocumento",20),"DNI","RUC"));
                entidad.setNroDocumento(Web.requerido(request,"nroDocumento",11));
                entidad.setNombresRazonSocial(Web.requerido(request,"nombresRazonSocial",150));
                entidad.setDireccion(Web.requerido(request,"direccion",200));
                entidad.setTelefono(Web.requerido(request,"telefono",20));
                entidad.setCorreo(Web.requerido(request,"correo",150));
                entidad.setEstado(Web.opcion(Web.requerido(request,"estado",20),"ACTIVO","INACTIVO"));
                int longitud="DNI".equals(entidad.getTipoDocumento())?8:11;
                if(!entidad.getNroDocumento().matches("[0-9]{"+longitud+"}")) throw new IllegalArgumentException("El documento debe tener "+longitud+" dígitos.");
                Web.correo(entidad.getCorreo());
                if(entidad.getIdCliente()==0) repositorio.guardar(entidad); else repositorio.actualizar(entidad);
            } else throw new IllegalArgumentException("Acción no válida.");
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) { log("Error al modificar clientes",e); Web.mensaje(request,e.getErrorCode()==1062?"El identificador único ya está registrado.":"No se pudo guardar o eliminar. Si el registro tiene movimientos, cámbialo a INACTIVO."); }
        response.sendRedirect(request.getContextPath()+"/clientes");
    }
}
