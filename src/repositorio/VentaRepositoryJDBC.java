// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class VentaRepositoryJDBC implements VentaRepository {
    private static final String CONSULTA="SELECT v.*,s.nro_solped,c.nombres_razon_social cliente,c.tipo_documento,c.nro_documento FROM venta v JOIN solped s ON s.id_solped=v.id_solped JOIN clientes c ON c.id_cliente=v.id_cliente";
    private Venta mapear(ResultSet r) throws SQLException {
        Venta v=new Venta(); v.setIdVenta(r.getInt("id_venta")); v.setNroVenta(r.getString("nro_venta"));
        v.setIdSolped(r.getInt("id_solped")); v.setNroSolped(r.getString("nro_solped")); v.setIdCliente(r.getInt("id_cliente"));
        v.setCliente(r.getString("cliente")); v.setTipoDocumento(r.getString("tipo_documento")); v.setNroDocumento(r.getString("nro_documento"));
        v.setFecha(r.getTimestamp("fecha")); v.setSubtotal(r.getBigDecimal("subtotal")); v.setIgv(r.getBigDecimal("igv")); v.setTotal(r.getBigDecimal("total"));
        v.setEstado(r.getString("estado")); v.setIdUsuario(r.getInt("id_usuario")); return v;
    }
    @Override public List<Venta> listar() throws SQLException {
        List<Venta> lista=new ArrayList<>();
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement(CONSULTA+" ORDER BY v.id_venta DESC"); ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        return lista;
    }
    @Override public Venta buscarPorId(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) { return buscar(c,id,false); }
    }
    Venta buscar(Connection c,int id,boolean bloquear) throws SQLException {
        Venta v;
        try(PreparedStatement p=c.prepareStatement(CONSULTA+" WHERE v.id_venta=?"+(bloquear?" FOR UPDATE":""))) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next()) return null; v=mapear(r); }
        }
        v.setDetalles(OperacionVentaJDBC.detalles(c,true,id)); return v;
    }
    static Venta exigirEmitida(Connection c,int id) throws SQLException {
        Venta v=new VentaRepositoryJDBC().buscar(c,id,true);
        if(v==null || !"EMITIDA".equals(v.getEstado())) throw new IllegalArgumentException("La venta debe estar emitida.");
        return v;
    }
    @Override public int crearDesdeSolped(Integer idSolped,Integer idUsuario) throws SQLException {
        return OperacionVentaJDBC.transaccion(c -> {
            Solped s=new SolpedRepositoryJDBC().buscar(c,idSolped,true);
            if(s==null || !"APROBADA".equals(s.getEstado())) throw new IllegalArgumentException("Solo una SOLPED APROBADA puede convertirse en venta.");
            OperacionVentaJDBC.clienteActivo(c,s.getIdCliente());
            if(s.getDetalles().isEmpty()) throw new IllegalArgumentException("La SOLPED no tiene productos.");
            OperacionVentaJDBC.moverStock(c,s.getDetalles(),true);
            BigDecimal subtotal=OperacionVentaJDBC.subtotal(s.getDetalles()), igv=OperacionVentaJDBC.igv(subtotal);
            int id;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO venta(nro_venta,id_solped,id_cliente,subtotal,igv,total,id_usuario) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,String.format("V-%06d",OperacionVentaJDBC.siguiente(c,"V")));
                p.setInt(2,idSolped); p.setInt(3,s.getIdCliente()); p.setBigDecimal(4,subtotal); p.setBigDecimal(5,igv); p.setBigDecimal(6,subtotal.add(igv)); p.setInt(7,idUsuario);
                p.executeUpdate(); id=OperacionVentaJDBC.generado(p);
            }
            OperacionVentaJDBC.guardarDetalles(c,true,id,s.getDetalles());
            try(PreparedStatement p=c.prepareStatement("UPDATE solped SET estado='ATENDIDA' WHERE id_solped=?")) { p.setInt(1,idSolped); p.executeUpdate(); }
            return id;
        });
    }
    @Override public void guardar(Venta venta) throws SQLException {
        venta.setIdVenta(crearDesdeSolped(venta.getIdSolped(),venta.getIdUsuario()));
    }
    @Override public void actualizar(Venta venta) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Venta actual=exigirEmitida(c,venta.getIdVenta());
            OperacionVentaJDBC.exigirSinDocumentos(c,actual.getIdVenta());
            OperacionVentaJDBC.clienteActivo(c,actual.getIdCliente());
            // El ajuste devuelve el detalle previo y descuenta el nuevo, dentro de la misma transacción.
            OperacionVentaJDBC.moverStock(c,actual.getDetalles(),false);
            OperacionVentaJDBC.preparar(c,venta.getDetalles());
            OperacionVentaJDBC.moverStock(c,venta.getDetalles(),true);
            BigDecimal subtotal=OperacionVentaJDBC.subtotal(venta.getDetalles()),igv=OperacionVentaJDBC.igv(subtotal);
            OperacionVentaJDBC.borrarDetalles(c,true,actual.getIdVenta());
            OperacionVentaJDBC.guardarDetalles(c,true,actual.getIdVenta(),venta.getDetalles());
            try(PreparedStatement p=c.prepareStatement("UPDATE venta SET subtotal=?,igv=?,total=? WHERE id_venta=?")) {
                p.setBigDecimal(1,subtotal); p.setBigDecimal(2,igv); p.setBigDecimal(3,subtotal.add(igv)); p.setInt(4,actual.getIdVenta()); p.executeUpdate();
            }
            return null;
        });
    }
    @Override public void anular(Integer id) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Venta actual=exigirEmitida(c,id);
            OperacionVentaJDBC.moverStock(c,actual.getDetalles(),false);
            try(PreparedStatement p=c.prepareStatement("UPDATE venta SET estado='ANULADA' WHERE id_venta=?")) { p.setInt(1,id); p.executeUpdate(); }
            try(PreparedStatement p=c.prepareStatement("UPDATE guia_remision SET estado='ANULADA' WHERE id_venta=?")) { p.setInt(1,id); p.executeUpdate(); }
            return null;
        });
    }
    @Override public void eliminar(Integer id) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Venta actual=buscar(c,id,true);
            if(actual==null || !"ANULADA".equals(actual.getEstado())) throw new IllegalArgumentException("Primero anula la venta para devolver el stock.");
            OperacionVentaJDBC.exigirSinDocumentos(c,id);
            try(PreparedStatement p=c.prepareStatement("DELETE FROM venta WHERE id_venta=?")) { p.setInt(1,id); p.executeUpdate(); }
            try(PreparedStatement p=c.prepareStatement("UPDATE solped SET estado='APROBADA' WHERE id_solped=?")) { p.setInt(1,actual.getIdSolped()); p.executeUpdate(); }
            return null;
        });
    }
}
