// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Consulta;
import modelo.Pagina;
import modelo.ReporteVentas;
import modelo.ReporteVentas.Punto;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ReporteRepositoryJDBC implements ReporteRepository {
    // %s recibe el filtro opcional por vendedor; todas las consultas usan el mismo rango de fechas.
    static final String PEDIDOS_ESTADO="SELECT s.estado, COUNT(*) FROM solped s WHERE DATE(s.fecha) BETWEEN ? AND ?%s GROUP BY s.estado";
    static final String VENTAS_ESTADO="SELECT v.estado, COUNT(*), COALESCE(SUM(v.total),0) FROM venta v WHERE DATE(v.fecha) BETWEEN ? AND ?%s GROUP BY v.estado";
    static final String POR_DIA="SELECT DATE(v.fecha), COUNT(*), SUM(v.total) FROM venta v WHERE v.estado='EMITIDA' AND DATE(v.fecha) BETWEEN ? AND ?%s GROUP BY DATE(v.fecha) ORDER BY DATE(v.fecha)";
    static final String PRODUCTOS="SELECT p.nombre, SUM(d.cantidad), SUM(d.subtotal) FROM venta_detalle d JOIN venta v ON v.id_venta=d.id_venta JOIN productos p ON p.id_producto=d.id_producto WHERE v.estado='EMITIDA' AND DATE(v.fecha) BETWEEN ? AND ?%s GROUP BY p.id_producto,p.nombre ORDER BY SUM(d.cantidad) DESC,p.nombre LIMIT 10";
    static final String CLIENTES="SELECT c.nombres_razon_social, COUNT(*), SUM(v.total) FROM venta v JOIN clientes c ON c.id_cliente=v.id_cliente WHERE v.estado='EMITIDA' AND DATE(v.fecha) BETWEEN ? AND ?%s GROUP BY c.id_cliente,c.nombres_razon_social ORDER BY SUM(v.total) DESC,c.nombres_razon_social LIMIT 10";
    static final String VENTAS="SELECT v.nro_venta, v.fecha, c.nombres_razon_social, u.usuario, v.estado, v.subtotal, v.igv, v.total FROM venta v JOIN clientes c ON c.id_cliente=v.id_cliente JOIN usuarios u ON u.id_usuario=v.id_usuario WHERE DATE(v.fecha) BETWEEN ? AND ?%s ORDER BY v.fecha DESC,v.id_venta DESC LIMIT 5000";
    static final String PEDIDOS="SELECT s.nro_solped, s.fecha, c.nombres_razon_social, u.usuario, s.estado, (SELECT COALESCE(SUM(d.subtotal),0) FROM solped_detalle d WHERE d.id_solped=s.id_solped) FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente JOIN usuarios u ON u.id_usuario=s.id_usuario WHERE DATE(s.fecha) BETWEEN ? AND ?%s ORDER BY s.fecha DESC,s.id_solped DESC LIMIT 5000";

    // Detalle paginado: mismo rango y vendedor que el reporte, sin el tope fijo de 5000 del Excel.
    static final String VENTAS_BASE="SELECT v.nro_venta, v.fecha, c.nombres_razon_social, u.usuario, v.estado, v.subtotal, v.igv, v.total FROM venta v JOIN clientes c ON c.id_cliente=v.id_cliente JOIN usuarios u ON u.id_usuario=v.id_usuario";
    static final String PEDIDOS_SUBTOTAL="(SELECT COALESCE(SUM(d.subtotal),0) FROM solped_detalle d WHERE d.id_solped=s.id_solped)";
    static final String PEDIDOS_BASE="SELECT s.nro_solped, s.fecha, c.nombres_razon_social, u.usuario, s.estado, "+PEDIDOS_SUBTOTAL+" FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente JOIN usuarios u ON u.id_usuario=s.id_usuario";
    private static final Map<String,String> COLUMNAS_VENTAS=Paginador.columnas("nro","v.nro_venta","fecha","v.fecha","cliente","c.nombres_razon_social","vendedor","u.usuario","estado","v.estado","subtotal","v.subtotal","igv","v.igv","total","v.total");
    private static final Map<String,String> COLUMNAS_PEDIDOS=Paginador.columnas("nro","s.nro_solped","fecha","s.fecha","cliente","c.nombres_razon_social","vendedor","u.usuario","estado","s.estado","subtotal",PEDIDOS_SUBTOTAL);
    private static Paginador detalle(String base,Map<String,String> columnas,String alias,String idDetalle,Integer vendedor,LocalDate desde,LocalDate hasta) {
        String filtro="DATE("+alias+".fecha) BETWEEN ? AND ?"+(vendedor==null?"":" AND "+alias+".id_usuario=?");
        java.sql.Date d=java.sql.Date.valueOf(desde),h=java.sql.Date.valueOf(hasta);
        return new Paginador(base,columnas,alias+".fecha DESC,"+idDetalle+" DESC",idDetalle).fijo(filtro,vendedor==null?new Object[]{d,h}:new Object[]{d,h,vendedor});
    }
    @Override public Pagina<Object[]> ventasDetalle(Integer idVendedor,LocalDate desde,LocalDate hasta,Consulta consulta) throws SQLException {
        return detalle(VENTAS_BASE,COLUMNAS_VENTAS,"v","v.id_venta",idVendedor,desde,hasta).buscar(consulta,r -> new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6),r.getBigDecimal(7),r.getBigDecimal(8)});
    }
    @Override public Pagina<Object[]> pedidosDetalle(Integer idVendedor,LocalDate desde,LocalDate hasta,Consulta consulta) throws SQLException {
        return detalle(PEDIDOS_BASE,COLUMNAS_PEDIDOS,"s","s.id_solped",idVendedor,desde,hasta).buscar(consulta,r -> new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6)});
    }
    private static PreparedStatement preparar(Connection c,String sql,String alias,Integer vendedor,LocalDate desde,LocalDate hasta) throws SQLException {
        PreparedStatement p=c.prepareStatement(String.format(sql,vendedor==null?"":" AND "+alias+".id_usuario=?"));
        p.setDate(1,java.sql.Date.valueOf(desde)); p.setDate(2,java.sql.Date.valueOf(hasta));
        if(vendedor!=null) p.setInt(3,vendedor);
        return p;
    }
    @Override public ReporteVentas ventas(Integer idVendedor,LocalDate desde,LocalDate hasta) throws SQLException {
        ReporteVentas reporte=new ReporteVentas(); reporte.setDesde(desde); reporte.setHasta(hasta);
        for(String estado:List.of("PENDIENTE","APROBADA","ATENDIDA","ANULADA")) reporte.getPedidosPorEstado().put(estado,0);
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            c.setAutoCommit(false); // una sola lectura consistente para todas las consultas
            try(PreparedStatement p=preparar(c,PEDIDOS_ESTADO,"s",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPedidosPorEstado().put(r.getString(1),r.getInt(2));
            }
            try(PreparedStatement p=preparar(c,VENTAS_ESTADO,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    if("EMITIDA".equals(r.getString(1))) { reporte.setVentasEmitidas(r.getInt(2)); reporte.setIngresos(r.getBigDecimal(3)); }
                    else { reporte.setVentasAnuladas(r.getInt(2)); reporte.setMontoAnulado(r.getBigDecimal(3)); }
                }
            }
            try(PreparedStatement p=preparar(c,POR_DIA,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPorDia().add(new Punto(r.getDate(1).toString(),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,PRODUCTOS,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getProductos().add(new Punto(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,CLIENTES,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getClientes().add(new Punto(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));
            }
            try(PreparedStatement p=preparar(c,VENTAS,"v",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getVentas().add(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6),r.getBigDecimal(7),r.getBigDecimal(8)});
            }
            try(PreparedStatement p=preparar(c,PEDIDOS,"s",idVendedor,desde,hasta); ResultSet r=p.executeQuery()) {
                while(r.next()) reporte.getPedidos().add(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBigDecimal(6)});
            }
            c.commit();
        }
        return reporte;
    }
}
