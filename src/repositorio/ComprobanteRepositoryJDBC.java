// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.*;
import java.sql.*;
import java.util.*;

public class ComprobanteRepositoryJDBC implements ComprobanteRepository {
    private static final String CONSULTA="SELECT d.*,v.nro_venta FROM comprobante d JOIN venta v ON v.id_venta=d.id_venta";
    private Comprobante mapear(ResultSet r) throws SQLException {
        Comprobante d=new Comprobante(); d.setIdComprobante(r.getInt("id_comprobante")); d.setTipo(r.getString("tipo"));
        d.setSerie(r.getString("serie")); d.setNumero(r.getInt("numero")); d.setIdVenta(r.getInt("id_venta"));
        d.setNroVenta(r.getString("nro_venta")); d.setFechaEmision(r.getTimestamp("fecha_emision")); d.setTotal(r.getBigDecimal("total")); return d;
    }
    @Override public List<Comprobante> listar() throws SQLException {
        List<Comprobante> lista=new ArrayList<>();
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement(CONSULTA+" ORDER BY d.id_comprobante DESC"); ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        return lista;
    }
    private Comprobante buscar(String columna,Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) { return buscar(c,columna,id,false); }
    }
    private Comprobante buscar(Connection c,String columna,Integer id,boolean bloquear) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(CONSULTA+" WHERE d."+columna+"=?"+(bloquear?" FOR UPDATE":""))) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    @Override public Comprobante buscarPorId(Integer id) throws SQLException { return buscar("id_comprobante",id); }
    @Override public Comprobante buscarPorVenta(Integer idVenta) throws SQLException { return buscar("id_venta",idVenta); }
    private String serie(Comprobante d,Venta venta) {
        if(!Set.of("FACTURA","BOLETA").contains(d.getTipo())) throw new IllegalArgumentException("Elige FACTURA o BOLETA.");
        if("FACTURA".equals(d.getTipo()) && (!"RUC".equals(venta.getTipoDocumento()) || !venta.getNroDocumento().matches("[0-9]{11}")))
            throw new IllegalArgumentException("La FACTURA exige un cliente con RUC de 11 dígitos.");
        return "FACTURA".equals(d.getTipo())?"F001":"B001";
    }
    private void sinGuia(Connection c,int idVenta) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT id_guia FROM guia_remision WHERE id_venta=?")) {
            p.setInt(1,idVenta); try(ResultSet r=p.executeQuery()) { if(r.next()) throw new IllegalArgumentException("Elimina primero la guía para modificar o eliminar el comprobante."); }
        }
    }
    @Override public void guardar(Comprobante d) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Venta venta=VentaRepositoryJDBC.exigirEmitida(c,d.getIdVenta());
            d.setSerie(serie(d,venta)); d.setNumero(OperacionVentaJDBC.siguiente(c,d.getSerie()));
            try(PreparedStatement p=c.prepareStatement("INSERT INTO comprobante(tipo,serie,numero,id_venta,total) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,d.getTipo()); p.setString(2,d.getSerie()); p.setInt(3,d.getNumero()); p.setInt(4,d.getIdVenta()); p.setBigDecimal(5,venta.getTotal());
                p.executeUpdate(); d.setIdComprobante(OperacionVentaJDBC.generado(p));
            }
            return null;
        });
    }
    @Override public void actualizar(Comprobante d) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Comprobante anterior=buscar(c,"id_comprobante",d.getIdComprobante(),false);
            if(anterior==null) throw new IllegalArgumentException("El comprobante no existe.");
            Venta venta=VentaRepositoryJDBC.exigirEmitida(c,anterior.getIdVenta()); sinGuia(c,venta.getIdVenta());
            anterior=buscar(c,"id_comprobante",d.getIdComprobante(),true);
            if(anterior==null) throw new IllegalArgumentException("El comprobante ya no existe.");
            String serie=serie(d,venta);
            int numero=serie.equals(anterior.getSerie())?anterior.getNumero():OperacionVentaJDBC.siguiente(c,serie);
            try(PreparedStatement p=c.prepareStatement("UPDATE comprobante SET tipo=?,serie=?,numero=? WHERE id_comprobante=?")) {
                p.setString(1,d.getTipo()); p.setString(2,serie); p.setInt(3,numero); p.setInt(4,d.getIdComprobante());
                if(p.executeUpdate()==0) throw new IllegalArgumentException("El comprobante ya no existe.");
            }
            return null;
        });
    }
    @Override public void eliminar(Integer id) throws SQLException {
        OperacionVentaJDBC.transaccion(c -> {
            Comprobante anterior=buscar(c,"id_comprobante",id,false);
            if(anterior==null) throw new IllegalArgumentException("El comprobante no existe.");
            Venta venta=new VentaRepositoryJDBC().buscar(c,anterior.getIdVenta(),true);
            if(venta==null) throw new IllegalArgumentException("La venta no existe.");
            sinGuia(c,venta.getIdVenta());
            try(PreparedStatement p=c.prepareStatement("DELETE FROM comprobante WHERE id_comprobante=?")) { p.setInt(1,id); p.executeUpdate(); }
            return null;
        });
    }
}
