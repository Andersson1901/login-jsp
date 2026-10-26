// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.Producto;
import repositorio.ProductoRepository;
import repositorio.Repositorios;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/productos")
public class ProductoServlet extends HttpServlet {
    private final ProductoRepository repositorio = Repositorios.productos();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.texto(request,"accion",20);
            if("nuevo".equals(accion)||"editar".equals(accion)) {
                Producto entidad="nuevo".equals(accion)?new Producto():repositorio.buscarPorId(Web.id(request,"id"));
                if(entidad==null) throw new IllegalArgumentException("El registro no existe.");
                request.setAttribute("registro",entidad);
                request.getRequestDispatcher("/WEB-INF/vistas/productos_formulario.jsp").forward(request,response);
            } else {
                String q=Web.texto(request,"q",100); request.setAttribute("q",q);
                request.setAttribute("registros",repositorio.listar(q));
                request.getRequestDispatcher("/WEB-INF/vistas/productos.jsp").forward(request,response);
            }
        } catch(SQLException e) {
            log("Error al consultar productos",e); request.setAttribute("error","No se pudo cargar la información.");
            request.getRequestDispatcher("/WEB-INF/vistas/productos.jsp").forward(request,response);
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+"/productos"); }
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try {
            String accion=Web.requerido(request,"accion",20);
            if("eliminar".equals(accion)) repositorio.eliminar(Web.id(request,"id"));
            else if("guardar".equals(accion)) {
                Producto entidad=new Producto();
                String id=Web.texto(request,"id",10); entidad.setIdProducto(id.isEmpty()?0:Web.id(request,"id"));
                entidad.setCodigo(Web.requerido(request,"codigo",30));
                entidad.setNombre(Web.requerido(request,"nombre",150));
                entidad.setDescripcion(Web.requerido(request,"descripcion",500));
                entidad.setTipo(Web.opcion(Web.requerido(request,"tipo",20),"MERCADERIA","PRODUCTO","INSUMO","SERVICIO"));
                entidad.setCategoria(Web.requerido(request,"categoria",80));
                entidad.setUnidadMedida(Web.requerido(request,"unidadMedida",20));
                entidad.setPrecioCompra(Web.decimal(request,"precioCompra",2));
                entidad.setPrecioVenta(Web.decimal(request,"precioVenta",2));
                entidad.setStock(Web.decimal(request,"stock",3));
                entidad.setStockMinimo(Web.decimal(request,"stockMinimo",3));
                entidad.setEstado(Web.opcion(Web.requerido(request,"estado",20),"ACTIVO","INACTIVO"));
                if("SERVICIO".equals(entidad.getTipo())) { entidad.setStock(BigDecimal.ZERO); entidad.setStockMinimo(BigDecimal.ZERO); }
                if(entidad.getIdProducto()==0) repositorio.guardar(entidad); else repositorio.actualizar(entidad);
            } else throw new IllegalArgumentException("Acción no válida.");
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) { log("Error al modificar productos",e); Web.mensaje(request,e.getErrorCode()==1062?"El identificador único ya está registrado.":"No se pudo guardar o eliminar. Si el registro tiene movimientos, cámbialo a INACTIVO."); }
        response.sendRedirect(request.getContextPath()+"/productos");
    }
}
