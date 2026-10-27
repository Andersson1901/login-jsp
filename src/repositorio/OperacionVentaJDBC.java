// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Detalle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

// Operaciones compartidas que reciben la MISMA conexión de la transacción.
final class OperacionVentaJDBC {
    private OperacionVentaJDBC() { }
    interface Trabajo<T> { T ejecutar(Connection c) throws SQLException; }

    static <T> T transaccion(Trabajo<T> trabajo) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion()) {
            // Tras esperar un bloqueo, las comprobaciones ven los documentos recién confirmados.
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            c.setAutoCommit(false);
            try {
                T resultado=trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch(SQLException | RuntimeException e) {
                try { c.rollback(); } catch(SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
    static int siguiente(Connection c,String serie) throws SQLException {
        int numero;
        try(PreparedStatement p=c.prepareStatement("SELECT ultimo FROM correlativos WHERE serie=? FOR UPDATE")) {
            p.setString(1,serie); try(ResultSet r=p.executeQuery()) {
                if(!r.next()) throw new SQLException("Serie no configurada");
                numero=Math.addExact(r.getInt(1),1);
            }
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE correlativos SET ultimo=? WHERE serie=?")) {
            p.setInt(1,numero); p.setString(2,serie); p.executeUpdate();
        }
        return numero;
    }
    static void clienteActivo(Connection c,int id) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT estado FROM clientes WHERE id_cliente=? LOCK IN SHARE MODE")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) {
                if(!r.next() || !"ACTIVO".equals(r.getString(1))) throw new IllegalArgumentException("Selecciona un cliente activo.");
            }
        }
    }
    static List<Detalle> detalles(Connection c,boolean venta,int id) throws SQLException {
        String tabla=venta?"venta_detalle":"solped_detalle", columna=venta?"id_venta":"id_solped";
        List<Detalle> lista=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement("SELECT d.*,p.nombre,p.tipo FROM "+tabla+" d JOIN productos p ON p.id_producto=d.id_producto WHERE d."+columna+"=? ORDER BY d.id_producto")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    Detalle d=new Detalle(); d.setId(r.getInt("id")); d.setIdProducto(r.getInt("id_producto"));
                    d.setNombreProducto(r.getString("nombre")); d.setTipo(r.getString("tipo"));
                    d.setCantidad(r.getBigDecimal("cantidad")); d.setPrecioUnitario(r.getBigDecimal("precio_unitario")); d.setSubtotal(r.getBigDecimal("subtotal")); lista.add(d);
                }
            }
        }
        return lista;
    }
    static void preparar(Connection c,List<Detalle> detalles) throws SQLException {
        if(detalles==null || detalles.isEmpty() || detalles.size()>100) throw new IllegalArgumentException("Agrega entre 1 y 100 productos.");
        detalles.sort(Comparator.comparingInt(Detalle::getIdProducto));
        Set<Integer> ids=new HashSet<>();
        for(Detalle d:detalles) {
            if(!ids.add(d.getIdProducto())) throw new IllegalArgumentException("No repitas productos; modifica su cantidad.");
            if(d.getCantidad()==null || d.getCantidad().signum()<=0 || d.getCantidad().scale()>3 || d.getCantidad().compareTo(new BigDecimal("999999"))>0)
                throw new IllegalArgumentException("Cada cantidad debe ser positiva y tener hasta 3 decimales.");
            try(PreparedStatement p=c.prepareStatement("SELECT nombre,tipo,precio_venta,estado FROM productos WHERE id_producto=? FOR UPDATE")) {
                p.setInt(1,d.getIdProducto()); try(ResultSet r=p.executeQuery()) {
                    if(!r.next() || !"ACTIVO".equals(r.getString("estado"))) throw new IllegalArgumentException("Selecciona productos activos.");
                    d.setNombreProducto(r.getString("nombre")); d.setTipo(r.getString("tipo")); d.setPrecioUnitario(r.getBigDecimal("precio_venta"));
                    d.setSubtotal(d.getCantidad().multiply(d.getPrecioUnitario()).setScale(2,RoundingMode.HALF_UP));
                }
            }
        }
    }
    static void guardarDetalles(Connection c,boolean venta,int id,List<Detalle> detalles) throws SQLException {
        String tabla=venta?"venta_detalle":"solped_detalle", columna=venta?"id_venta":"id_solped";
        try(PreparedStatement p=c.prepareStatement("INSERT INTO "+tabla+"("+columna+",id_producto,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)")) {
            for(Detalle d:detalles) { p.setInt(1,id); p.setInt(2,d.getIdProducto()); p.setBigDecimal(3,d.getCantidad()); p.setBigDecimal(4,d.getPrecioUnitario()); p.setBigDecimal(5,d.getSubtotal()); p.addBatch(); }
            p.executeBatch();
        }
    }
    static void borrarDetalles(Connection c,boolean venta,int id) throws SQLException {
        String tabla=venta?"venta_detalle":"solped_detalle", columna=venta?"id_venta":"id_solped";
        try(PreparedStatement p=c.prepareStatement("DELETE FROM "+tabla+" WHERE "+columna+"=?")) { p.setInt(1,id); p.executeUpdate(); }
    }
    static BigDecimal subtotal(List<Detalle> detalles) {
        BigDecimal total=detalles.stream().map(Detalle::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);
        if(total.compareTo(new BigDecimal("99999999999"))>0) throw new IllegalArgumentException("El importe supera el límite permitido.");
        return total;
    }
    static BigDecimal igv(BigDecimal subtotal) { return subtotal.multiply(new BigDecimal("0.18")).setScale(2,RoundingMode.HALF_UP); }
    static void moverStock(Connection c,List<Detalle> detalles,boolean descontar) throws SQLException {
        detalles.sort(Comparator.comparingInt(Detalle::getIdProducto));
        for(Detalle d:detalles) {
            try(PreparedStatement p=c.prepareStatement("SELECT stock,tipo,estado FROM productos WHERE id_producto=? FOR UPDATE")) {
                p.setInt(1,d.getIdProducto()); try(ResultSet r=p.executeQuery()) {
                    if(!r.next()) throw new IllegalArgumentException("El producto ya no existe.");
                    if(descontar && !"ACTIVO".equals(r.getString("estado"))) throw new IllegalArgumentException("Hay productos inactivos en el pedido.");
                    if("SERVICIO".equals(r.getString("tipo"))) continue;
                    BigDecimal stock=r.getBigDecimal("stock");
                    if(descontar && stock.compareTo(d.getCantidad())<0) throw new IllegalArgumentException("Stock insuficiente para "+d.getNombreProducto()+".");
                    try(PreparedStatement cambio=c.prepareStatement("UPDATE productos SET stock=? WHERE id_producto=?")) {
                        cambio.setBigDecimal(1,descontar?stock.subtract(d.getCantidad()):stock.add(d.getCantidad())); cambio.setInt(2,d.getIdProducto()); cambio.executeUpdate();
                    }
                }
            }
        }
    }
    static void exigirSinDocumentos(Connection c,int idVenta) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM comprobante WHERE id_venta=?)+(SELECT COUNT(*) FROM guia_remision WHERE id_venta=?)")) {
            p.setInt(1,idVenta); p.setInt(2,idVenta); try(ResultSet r=p.executeQuery()) {
                r.next(); if(r.getInt(1)>0) throw new IllegalArgumentException("La venta ya tiene documentos. Elimínalos primero si fue un error de registro.");
            }
        }
    }
    static int generado(PreparedStatement p) throws SQLException {
        try(ResultSet r=p.getGeneratedKeys()) { if(!r.next()) throw new SQLException("No se generó el identificador"); return r.getInt(1); }
    }
}
