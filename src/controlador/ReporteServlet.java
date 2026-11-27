// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;
import modelo.ReporteVentas;
import modelo.ReporteVentas.Punto;
import modelo.Usuario;
import repositorio.ReporteRepository;
import repositorio.Repositorios;
import repositorio.UsuarioRepository;
import util.Excel;
import util.Paginacion;
import util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Reporte de ventas y pedidos. El VENDEDOR solo ve lo suyo; ADMIN y DUENO eligen un vendedor o toda la tienda.
@WebServlet("/reportes")
public class ReporteServlet extends HttpServlet {
    private static final int MAXIMO_DIAS = 366;
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ReporteRepository reportes = Repositorios.reportes();
    private final UsuarioRepository usuarios = Repositorios.usuarios();

    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        HttpSession sesion = request.getSession();
        boolean elige = !"VENDEDOR".equals(sesion.getAttribute("rol"));
        LocalDate hoy = LocalDate.now(ZoneOffset.ofHours(-5)); // misma zona horaria que usa ConexionBD
        LocalDate desde = hoy.minusDays(29), hasta = hoy;
        Integer vendedor = elige ? null : (Integer) sesion.getAttribute("idUsuario");
        try {
            if (elige) request.setAttribute("vendedores",vendedores()); // el selector se muestra aunque haya un error de fechas
            if (!Web.texto(request,"desde",10).isEmpty()) desde = fecha(Web.texto(request,"desde",10));
            if (!Web.texto(request,"hasta",10).isEmpty()) hasta = fecha(Web.texto(request,"hasta",10));
            if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
            if (ChronoUnit.DAYS.between(desde,hasta) >= MAXIMO_DIAS) throw new IllegalArgumentException("El rango máximo es de " + MAXIMO_DIAS + " días.");
            if (elige && !Web.texto(request,"vendedor",10).isEmpty()) vendedor = Web.id(request,"vendedor");
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            ReporteVentas reporte = null;
            if (vendedor != null) {
                Usuario u = usuarios.buscarPorId(vendedor);
                if (u == null || !Set.of("ADMIN","DUENO","VENDEDOR").contains(u.getRol())) throw new IllegalArgumentException("Selecciona un vendedor válido.");
                reporte = reportes.ventas(vendedor,desde,hasta);
                reporte.setVendedor(u.getNombres() + " " + u.getApellidos() + " (" + u.getUsuario() + ")");
            } else reporte = reportes.ventas(null,desde,hasta);
            if ("xlsx".equals(Web.texto(request,"formato",10))) { descargar(response,reporte); return; }
            request.setAttribute("ventasPagina",reportes.ventasDetalle(vendedor,desde,hasta,Paginacion.consulta(request,"v_")));
            request.setAttribute("pedidosPagina",reportes.pedidosDetalle(vendedor,desde,hasta,Paginacion.consulta(request,"p_")));
            request.setAttribute("reporte",reporte);
        } catch (IllegalArgumentException e) {
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            request.setAttribute("error",e.getMessage());
        } catch (SQLException e) {
            log("Error al generar el reporte de ventas",e);
            request.setAttribute("desde",desde); request.setAttribute("hasta",hasta); request.setAttribute("vendedor",vendedor);
            request.setAttribute("error","No se pudo generar el reporte. Intenta nuevamente.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/reportes.jsp").forward(request,response);
    }

    private static LocalDate fecha(String valor) {
        try { return LocalDate.parse(valor); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Escribe fechas válidas."); }
    }

    private List<Usuario> vendedores() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        for (Usuario u : usuarios.listar("")) if (Set.of("ADMIN","DUENO","VENDEDOR").contains(u.getRol())) lista.add(u);
        return lista;
    }

    // Mismos datos que se ven en pantalla, uno por hoja.
    private void descargar(HttpServletResponse response,ReporteVentas r) throws IOException {
        Map<String,List<Object[]>> hojas = new LinkedHashMap<>();
        List<Object[]> resumen = new ArrayList<>();
        resumen.add(new Object[]{"Dato","Valor"});
        resumen.add(new Object[]{"Alcance", r.getVendedor() == null ? "Toda la tienda" : r.getVendedor()});
        resumen.add(new Object[]{"Desde", r.getDesde().toString()});
        resumen.add(new Object[]{"Hasta", r.getHasta().toString()});
        for (Map.Entry<String,Integer> e : r.getPedidosPorEstado().entrySet()) resumen.add(new Object[]{"Pedidos " + e.getKey(), e.getValue()});
        resumen.add(new Object[]{"Pedidos (total)", r.getTotalPedidos()});
        resumen.add(new Object[]{"Ventas emitidas", r.getVentasEmitidas()});
        resumen.add(new Object[]{"Ingresos (S/)", r.getIngresos()});
        resumen.add(new Object[]{"Ticket promedio (S/)", r.getTicketPromedio()});
        resumen.add(new Object[]{"Ventas anuladas", r.getVentasAnuladas()});
        resumen.add(new Object[]{"Monto anulado (S/)", r.getMontoAnulado()});
        hojas.put("Resumen",resumen);
        hojas.put("Ventas por día",puntos("Fecha","Ventas","Importe (S/)",r.getPorDia(),true));
        hojas.put("Productos",puntos("Producto","Unidades","Importe (S/)",r.getProductos(),false));
        hojas.put("Clientes",puntos("Cliente","Compras","Importe (S/)",r.getClientes(),true));
        List<Object[]> ventas = new ArrayList<>();
        ventas.add(new Object[]{"N° venta","Fecha","Cliente","Vendedor","Estado","Subtotal (S/)","IGV (S/)","Total (S/)"});
        ventas.addAll(r.getVentas());
        hojas.put("Ventas",ventas);
        List<Object[]> pedidos = new ArrayList<>();
        pedidos.add(new Object[]{"N° SOLPED","Fecha","Cliente","Vendedor","Estado","Subtotal (S/)"});
        pedidos.addAll(r.getPedidos());
        hojas.put("Pedidos",pedidos);
        response.setContentType(XLSX);
        response.setHeader("Content-Disposition","attachment; filename=\"reporte-ventas-" + r.getDesde() + "_" + r.getHasta() + ".xlsx\"");
        Excel.escribir(response.getOutputStream(),hojas);
    }

    private static List<Object[]> puntos(String etiqueta,String cantidad,String importe,List<Punto> datos,boolean entera) {
        List<Object[]> filas = new ArrayList<>();
        filas.add(new Object[]{etiqueta,cantidad,importe});
        for (Punto p : datos) filas.add(new Object[]{p.getEtiqueta(), entera ? (Object) p.getCantidad().intValue() : p.getCantidad(), p.getImporte()});
        return filas;
    }
}
