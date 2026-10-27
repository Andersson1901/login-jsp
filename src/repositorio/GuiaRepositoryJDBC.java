// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.sql.*;
import java.util.*;

public class GuiaRepositoryJDBC implements GuiaRepository {
    private static final String CONSULTA="SELECT g.*,v.nro_venta FROM guia_remision g JOIN venta v ON v.id_venta=g.id_venta";
    private GuiaRemision mapear(ResultSet r) throws SQLException {
        GuiaRemision g=new GuiaRemision(); g.setIdGuia(r.getInt("id_guia")); g.setSerie(r.getString("serie")); g.setNumero(r.getInt("numero"));
        g.setIdVenta(r.getInt("id_venta")); g.setNroVenta(r.getString("nro_venta")); g.setFechaTraslado(r.getDate("fecha_traslado"));
        g.setPuntoPartida(r.getString("punto_partida")); g.setPuntoLlegada(r.getString("punto_llegada")); g.setEstado(r.getString("estado")); return g;
    }
    @Override public List<GuiaRemision> listar() throws SQLException {
        List<GuiaRemision> lista=new ArrayList<>();
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement(CONSULTA+" ORDER BY g.id_guia DESC"); ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        return lista;
    }
    private GuiaRemision buscar(String columna,Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) { return buscar(c,columna,id,false); }
    }
    private GuiaRemision buscar(Connection c,String columna,Integer id,boolean bloquear) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(CONSULTA+" WHERE g."+columna+"=?"+(bloquear?" FOR UPDATE":""))) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    @Override public GuiaRemision buscarPorId(Integer id) throws SQLException { return buscar("id_guia",id); }
    @Override public GuiaRemision buscarPorVenta(Integer id) throws SQLException { return buscar("id_venta",id); }
    private void validar(GuiaRemision g) {
        if(g.getFechaTraslado()==null || g.getPuntoPartida()==null || g.getPuntoPartida().isBlank() || g.getPuntoLlegada()==null || g.getPuntoLlegada().isBlank()) throw new IllegalArgumentException("Completa fecha y puntos de traslado.");
        if(g.getPuntoPartida().length()>200 || g.getPuntoLlegada().length()>200) throw new IllegalArgumentException("Las direcciones admiten hasta 200 caracteres.");
        if(!Set.of("PENDIENTE","ENTREGADA","ANULADA").contains(g.getEstado())) throw new IllegalArgumentException("Estado de guía no válido.");
    }
    @Override public void guardar(GuiaRemision g) throws SQLException {
        validar(g);
        OperacionVentaJDBC.transaccion(c -> {
            Venta v=VentaRepositoryJDBC.exigirEmitida(c,g.getIdVenta());
            try(PreparedStatement p=c.prepareStatement("SELECT id_comprobante FROM comprobante WHERE id_venta=?")) {
                p.setInt(1,v.getIdVenta()); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalArgumentException("Emite el comprobante antes de crear la guía."); }
            }
            g.setNumero(OperacionVentaJDBC.siguiente(c,"T001"));
            try(PreparedStatement p=c.prepareStatement("INSERT INTO guia_remision(serie,numero,id_venta,fecha_traslado,punto_partida,punto_llegada,estado) VALUES('T001',?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1,g.getNumero()); p.setInt(2,g.getIdVenta()); p.setDate(3,g.getFechaTraslado()); p.setString(4,g.getPuntoPartida()); p.setString(5,g.getPuntoLlegada()); p.setString(6,g.getEstado());
                p.executeUpdate(); g.setIdGuia(OperacionVentaJDBC.generado(p));
            }
            return null;
        });
    }
    @Override public void actualizar(GuiaRemision g) throws SQLException {
        validar(g);
        OperacionVentaJDBC.transaccion(c -> {
            GuiaRemision anterior=buscar(c,"id_guia",g.getIdGuia(),false);
            if(anterior==null) throw new IllegalArgumentException("La guía no existe.");
            VentaRepositoryJDBC.exigirEmitida(c,anterior.getIdVenta());
            try(PreparedStatement p=c.prepareStatement("UPDATE guia_remision SET fecha_traslado=?,punto_partida=?,punto_llegada=?,estado=? WHERE id_guia=?")) {
                p.setDate(1,g.getFechaTraslado()); p.setString(2,g.getPuntoPartida()); p.setString(3,g.getPuntoLlegada()); p.setString(4,g.getEstado()); p.setInt(5,g.getIdGuia());
                if(p.executeUpdate()==0) throw new IllegalArgumentException("La guía ya no existe.");
            }
            return null;
        });
    }
    @Override public void eliminar(Integer id) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            GuiaRemision g=buscar(c,"id_guia",id,false);
            if(g==null) throw new IllegalArgumentException("La guía no existe.");
            new VentaRepositoryJDBC().buscar(c,g.getIdVenta(),true);
            try(PreparedStatement p=c.prepareStatement("DELETE FROM guia_remision WHERE id_guia=?")) { p.setInt(1,id); p.executeUpdate(); }
            return null;
        });
    }
}
