// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Solped;
import java.sql.*;
import java.util.*;

public class SolpedRepositoryJDBC implements SolpedRepository {
    private static final String CONSULTA="SELECT s.*,c.nombres_razon_social cliente FROM solped s JOIN clientes c ON c.id_cliente=s.id_cliente";
    private Solped mapear(ResultSet r) throws SQLException {
        Solped s=new Solped(); s.setIdSolped(r.getInt("id_solped")); s.setNroSolped(r.getString("nro_solped"));
        s.setIdCliente(r.getInt("id_cliente")); s.setCliente(r.getString("cliente")); s.setFecha(r.getTimestamp("fecha"));
        s.setEstado(r.getString("estado")); s.setObservacion(r.getString("observacion")); s.setIdUsuario(r.getInt("id_usuario")); return s;
    }
    @Override public List<Solped> listar() throws SQLException {
        List<Solped> lista=new ArrayList<>();
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement(CONSULTA+" ORDER BY s.id_solped DESC"); ResultSet r=p.executeQuery()) {
            while(r.next()) lista.add(mapear(r));
        }
        return lista;
    }
    @Override public Solped buscarPorId(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) { return buscar(c,id,false); }
    }
    Solped buscar(Connection c,int id,boolean bloquear) throws SQLException {
        Solped s;
        try(PreparedStatement p=c.prepareStatement(CONSULTA+" WHERE s.id_solped=?"+(bloquear?" FOR UPDATE":""))) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next()) return null; s=mapear(r); }
        }
        s.setDetalles(OperacionVentaJDBC.detalles(c,false,id)); return s;
    }
    @Override public void guardar(Solped s) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            OperacionVentaJDBC.clienteActivo(c,s.getIdCliente());
            OperacionVentaJDBC.preparar(c,s.getDetalles());
            s.setNroSolped(String.format("SP-%06d",OperacionVentaJDBC.siguiente(c,"SP")));
            try(PreparedStatement p=c.prepareStatement("INSERT INTO solped(nro_solped,id_cliente,observacion,id_usuario) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,s.getNroSolped()); p.setInt(2,s.getIdCliente()); p.setString(3,s.getObservacion()); p.setInt(4,s.getIdUsuario());
                p.executeUpdate(); s.setIdSolped(OperacionVentaJDBC.generado(p));
            }
            OperacionVentaJDBC.guardarDetalles(c,false,s.getIdSolped(),s.getDetalles()); return null;
        });
    }
    @Override public void actualizar(Solped s) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Solped actual=buscar(c,s.getIdSolped(),true);
            if(actual==null || !"PENDIENTE".equals(actual.getEstado())) throw new IllegalArgumentException("Solo se editan SOLPED pendientes.");
            OperacionVentaJDBC.clienteActivo(c,s.getIdCliente()); OperacionVentaJDBC.preparar(c,s.getDetalles());
            try(PreparedStatement p=c.prepareStatement("UPDATE solped SET id_cliente=?,observacion=? WHERE id_solped=?")) {
                p.setInt(1,s.getIdCliente()); p.setString(2,s.getObservacion()); p.setInt(3,s.getIdSolped()); p.executeUpdate();
            }
            OperacionVentaJDBC.borrarDetalles(c,false,s.getIdSolped());
            OperacionVentaJDBC.guardarDetalles(c,false,s.getIdSolped(),s.getDetalles()); return null;
        });
    }
    @Override public void cambiarEstado(Integer id,String estado) throws SQLException {
        if(!Set.of("PENDIENTE","APROBADA","ANULADA").contains(estado)) throw new IllegalArgumentException("Estado no válido.");
        OperacionVentaJDBC.transaccion(c -> {
            Solped actual=buscar(c,id,true);
            if(actual==null || !Set.of("PENDIENTE","APROBADA").contains(actual.getEstado())) throw new IllegalArgumentException("La SOLPED no permite este cambio de estado.");
            if("APROBADA".equals(estado)) {
                OperacionVentaJDBC.clienteActivo(c,actual.getIdCliente());
                if(actual.getDetalles().isEmpty()) throw new IllegalArgumentException("La SOLPED no tiene detalle.");
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE solped SET estado=? WHERE id_solped=?")) { p.setString(1,estado); p.setInt(2,id); p.executeUpdate(); }
            return null;
        });
    }
    @Override public void eliminar(Integer id) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Solped actual=buscar(c,id,true);
            if(actual==null || !Set.of("PENDIENTE","ANULADA").contains(actual.getEstado())) throw new IllegalArgumentException("Solo se eliminan SOLPED pendientes o anuladas sin venta.");
            try(PreparedStatement p=c.prepareStatement("DELETE FROM solped WHERE id_solped=?")) { p.setInt(1,id); p.executeUpdate(); }
            return null;
        });
    }
}
