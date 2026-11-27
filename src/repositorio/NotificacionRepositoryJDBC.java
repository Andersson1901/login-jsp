// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Consulta;
import modelo.Pagina;
import modelo.Notificacion;
import modelo.Venta;
import java.sql.*;
import java.util.*;

public class NotificacionRepositoryJDBC implements NotificacionRepository {
    private Notificacion mapear(ResultSet r) throws SQLException {
        Notificacion n=new Notificacion();
        n.setIdNotificacion(r.getInt("id_notificacion")); n.setIdUsuario(r.getInt("id_usuario")); n.setIdVenta(r.getInt("id_venta"));
        n.setNroVenta(r.getString("nro_venta")); n.setCliente(r.getString("cliente"));
        n.setEstadoAnterior(r.getString("estado_anterior")); n.setEstadoNuevo(r.getString("estado_nuevo"));
        n.setTotal(r.getBigDecimal("total")); n.setFecha(r.getTimestamp("fecha")); n.setLeida(r.getBoolean("leida"));
        return n;
    }
    // Una sola sentencia crea el aviso de cada dueño activo; el proyecto no separa las tiendas.
    @Override public void notificarDuenos(Venta venta,String estadoAnterior,String estadoNuevo) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO notificacion(id_usuario,id_venta,nro_venta,cliente,estado_anterior,estado_nuevo,total) SELECT id_usuario,?,?,?,?,?,? FROM usuarios WHERE rol='DUENO' AND estado='ACTIVO'")) {
            p.setInt(1,venta.getIdVenta()); p.setString(2,venta.getNroVenta()); p.setString(3,venta.getCliente());
            if(estadoAnterior==null) p.setNull(4,Types.VARCHAR); else p.setString(4,estadoAnterior);
            p.setString(5,estadoNuevo); p.setBigDecimal(6,venta.getTotal()); p.executeUpdate();
        }
    }
    private static final Map<String,String> COLUMNAS=Paginador.columnas("fecha","fecha","venta","nro_venta","cliente","cliente","total","total","cambio","estado_nuevo");
    @Override public Pagina<Notificacion> buscar(int idUsuario,Consulta consulta) throws SQLException {
        return new Paginador("SELECT * FROM notificacion",COLUMNAS,"fecha DESC,id_notificacion DESC","id_notificacion").fijo("id_usuario=?",idUsuario).buscar(consulta,this::mapear);
    }
    @Override public List<Notificacion> listarPorUsuario(int idUsuario) throws SQLException {
        List<Notificacion> lista=new ArrayList<>();
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM notificacion WHERE id_usuario=? ORDER BY fecha DESC,id_notificacion DESC LIMIT 100")) {
            p.setInt(1,idUsuario); try(ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
    @Override public int contarNoLeidas(int idUsuario) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM notificacion WHERE id_usuario=? AND leida=0")) {
            p.setInt(1,idUsuario); try(ResultSet r=p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }
    @Override public void marcarLeidas(int idUsuario) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE notificacion SET leida=1 WHERE id_usuario=? AND leida=0")) {
            p.setInt(1,idUsuario); p.executeUpdate();
        }
    }
}
