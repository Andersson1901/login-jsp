// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import modelo.*;
import repositorio.*;
import util.Web;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet({"/solped","/ventas","/comprobantes","/guias"})
public class VentaServlet extends HttpServlet {
    private final SolpedRepository solicitudes=Repositorios.solped();
    private final VentaRepository ventas=Repositorios.ventas();
    private final ComprobanteRepository comprobantes=Repositorios.comprobantes();
    private final GuiaRepository guias=Repositorios.guias();
    private final ClienteRepository clientes=Repositorios.clientes();
    private final ProductoRepository productos=Repositorios.productos();

    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        String ruta=request.getServletPath();
        try {
            String accion=Web.texto(request,"accion",20);
            request.setAttribute("ruta",ruta);
            if(("/solped".equals(ruta) && Set.of("nuevo","editar").contains(accion)) || ("/ventas".equals(ruta) && "editar".equals(accion))) {
                boolean esVenta="/ventas".equals(ruta); Object registro;
                if(esVenta) registro=ventas.buscarPorId(Web.id(request,"id"));
                else registro="nuevo".equals(accion)?new Solped():solicitudes.buscarPorId(Web.id(request,"id"));
                if(registro==null) throw new IllegalArgumentException("El registro no existe.");
                request.setAttribute("esVenta",esVenta); request.setAttribute("registro",registro);
                request.setAttribute("clientes",clientes.listar()); request.setAttribute("productos",productos.listar());
                ver(request,response,"pedido_formulario.jsp");
            } else if(("/solped".equals(ruta) || "/ventas".equals(ruta)) && "ver".equals(accion)) {
                boolean esVenta="/ventas".equals(ruta); int id=Web.id(request,"id");
                Object registro=esVenta?ventas.buscarPorId(id):solicitudes.buscarPorId(id);
                if(registro==null) throw new IllegalArgumentException("El registro no existe.");
                request.setAttribute("esVenta",esVenta); request.setAttribute("registro",registro);
                if(esVenta) { request.setAttribute("comprobante",comprobantes.buscarPorVenta(id)); request.setAttribute("guia",guias.buscarPorVenta(id)); }
                ver(request,response,"pedido_detalle.jsp");
            } else if(("/comprobantes".equals(ruta)||"/guias".equals(ruta)) && Set.of("nuevo","editar").contains(accion)) {
                boolean esGuia="/guias".equals(ruta); boolean nuevo="nuevo".equals(accion);
                Object registro=esGuia?(nuevo?new GuiaRemision():guias.buscarPorId(Web.id(request,"id"))):(nuevo?new Comprobante():comprobantes.buscarPorId(Web.id(request,"id")));
                if(registro==null) throw new IllegalArgumentException("El documento no existe.");
                if(nuevo && request.getParameter("idVenta")!=null) {
                    int idVenta=Web.id(request,"idVenta");
                    if(esGuia) ((GuiaRemision)registro).setIdVenta(idVenta); else ((Comprobante)registro).setIdVenta(idVenta);
                }
                request.setAttribute("esGuia",esGuia); request.setAttribute("registro",registro);
                request.setAttribute("ventas",ventas.listar()); ver(request,response,"documento_formulario.jsp");
            } else {
                switch(ruta) {
                    case "/solped": request.setAttribute("registros",solicitudes.listar()); break;
                    case "/ventas": request.setAttribute("registros",ventas.listar()); break;
                    case "/comprobantes": request.setAttribute("registros",comprobantes.listar()); break;
                    case "/guias": request.setAttribute("registros",guias.listar()); break;
                    default: throw new IllegalArgumentException("Módulo no válido.");
                }
                ver(request,response,ruta.substring(1)+"_lista.jsp");
            }
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+ruta); }
        catch(SQLException e) { log("Error al consultar ventas",e); request.setAttribute("error","No se pudo cargar la información. Intenta nuevamente."); ver(request,response,"ventas_error.jsp"); }
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        String ruta=request.getServletPath(),destino=ruta;
        try {
            String accion=Web.requerido(request,"accion",20);
            int usuario=(Integer)request.getSession().getAttribute("idUsuario");
            switch(ruta) {
                case "/solped":
                    if("guardar".equals(accion)) {
                        Solped s=new Solped(); s.setIdSolped(idOpcional(request)); s.setIdCliente(Web.id(request,"idCliente")); s.setIdUsuario(usuario);
                        s.setObservacion(Web.texto(request,"observacion",500)); s.setDetalles(detalles(request));
                        if(s.getIdSolped()==0) solicitudes.guardar(s); else solicitudes.actualizar(s);
                        destino="/solped?accion=ver&id="+s.getIdSolped();
                    } else if("estado".equals(accion)) solicitudes.cambiarEstado(Web.id(request,"id"),Web.opcion(Web.requerido(request,"estado",20),"PENDIENTE","APROBADA","ANULADA"));
                    else if("eliminar".equals(accion)) solicitudes.eliminar(Web.id(request,"id"));
                    else throw new IllegalArgumentException("Acción no válida.");
                    break;
                case "/ventas":
                    if("convertir".equals(accion)) destino="/ventas?accion=ver&id="+ventas.crearDesdeSolped(Web.id(request,"id"),usuario);
                    else if("guardar".equals(accion)) {
                        Venta v=new Venta(); v.setIdVenta(Web.id(request,"id")); v.setDetalles(detalles(request)); ventas.actualizar(v); destino="/ventas?accion=ver&id="+v.getIdVenta();
                    } else if("anular".equals(accion)) ventas.anular(Web.id(request,"id"));
                    else if("eliminar".equals(accion)) ventas.eliminar(Web.id(request,"id"));
                    else throw new IllegalArgumentException("Acción no válida.");
                    break;
                case "/comprobantes":
                    if("guardar".equals(accion)) {
                        Comprobante d=new Comprobante(); d.setIdComprobante(idOpcional(request)); d.setIdVenta(Web.id(request,"idVenta"));
                        d.setTipo(Web.opcion(Web.requerido(request,"tipo",10),"FACTURA","BOLETA"));
                        if(d.getIdComprobante()==0) comprobantes.guardar(d); else comprobantes.actualizar(d);
                    } else if("eliminar".equals(accion)) comprobantes.eliminar(Web.id(request,"id"));
                    else throw new IllegalArgumentException("Acción no válida.");
                    break;
                case "/guias":
                    if("guardar".equals(accion)) {
                        GuiaRemision g=new GuiaRemision(); g.setIdGuia(idOpcional(request)); g.setIdVenta(Web.id(request,"idVenta"));
                        try {
                            LocalDate fecha=LocalDate.parse(Web.requerido(request,"fechaTraslado",10));
                            if(fecha.getYear()<1900 || fecha.getYear()>9999) throw new IllegalArgumentException("Fecha de traslado no válida.");
                            g.setFechaTraslado(java.sql.Date.valueOf(fecha));
                        } catch(DateTimeParseException e) { throw new IllegalArgumentException("Fecha de traslado no válida."); }
                        g.setPuntoPartida(Web.requerido(request,"puntoPartida",200)); g.setPuntoLlegada(Web.requerido(request,"puntoLlegada",200));
                        g.setEstado(Web.opcion(Web.requerido(request,"estado",20),"PENDIENTE","ENTREGADA","ANULADA"));
                        if(g.getIdGuia()==0) guias.guardar(g); else guias.actualizar(g);
                    } else if("eliminar".equals(accion)) guias.eliminar(Web.id(request,"id"));
                    else throw new IllegalArgumentException("Acción no válida.");
                    break;
                default: throw new IllegalArgumentException("Módulo no válido.");
            }
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch(IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch(SQLException e) {
            log("Error en la operación de ventas",e);
            Web.mensaje(request,e.getErrorCode()==1062?"La operación ya fue registrada. Solo se permite una venta por SOLPED, un comprobante y una guía por venta.":"No se pudo completar la operación. No se guardaron cambios; revisa los datos e intenta nuevamente.");
        }
        response.sendRedirect(request.getContextPath()+destino);
    }
    private int idOpcional(HttpServletRequest request) { return Web.texto(request,"id",10).isEmpty()?0:Web.id(request,"id"); }
    private List<Detalle> detalles(HttpServletRequest request) {
        String[] ids=request.getParameterValues("idProducto"), cantidades=request.getParameterValues("cantidad");
        if(ids==null || cantidades==null || ids.length!=cantidades.length || ids.length==0 || ids.length>100) throw new IllegalArgumentException("Agrega entre 1 y 100 ítems completos.");
        List<Detalle> lista=new ArrayList<>();
        try {
            for(int i=0;i<ids.length;i++) {
                if(ids[i].length()>10 || cantidades[i].length()>20) throw new NumberFormatException();
                Detalle d=new Detalle(); d.setIdProducto(Integer.parseInt(ids[i])); d.setCantidad(new BigDecimal(cantidades[i])); lista.add(d);
            }
        } catch(NumberFormatException e) { throw new IllegalArgumentException("Revisa los productos y cantidades."); }
        return lista;
    }
    private void ver(HttpServletRequest request,HttpServletResponse response,String jsp) throws ServletException,IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/"+jsp).forward(request,response);
    }
}
