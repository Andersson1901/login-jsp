// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Producto;
import java.sql.*;
import java.util.*;

public class ProductoRepositoryJDBC implements ProductoRepository {
    private Producto mapear(ResultSet r) throws SQLException {
        Producto entidad = new Producto();
        entidad.setIdProducto(r.getInt("id_producto"));
        entidad.setCodigo(r.getString("codigo"));
        entidad.setNombre(r.getString("nombre"));
        entidad.setDescripcion(r.getString("descripcion"));
        entidad.setTipo(r.getString("tipo"));
        entidad.setCategoria(r.getString("categoria"));
        entidad.setUnidadMedida(r.getString("unidad_medida"));
        entidad.setPrecioCompra(r.getBigDecimal("precio_compra"));
        entidad.setPrecioVenta(r.getBigDecimal("precio_venta"));
        entidad.setStock(r.getBigDecimal("stock"));
        entidad.setStockMinimo(r.getBigDecimal("stock_minimo"));
        entidad.setEstado(r.getString("estado"));
        return entidad;
    }
    @Override public List<Producto> listar() throws SQLException { return listar(""); }
    @Override public List<Producto> listar(String busqueda) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM productos WHERE codigo LIKE ? OR nombre LIKE ? OR categoria LIKE ? ORDER BY id_producto")) {
            for(int i=1;i<=3;i++) p.setString(i,"%"+busqueda+"%");
            try(ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
    @Override public Producto buscarPorId(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM productos WHERE id_producto=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    private void parametros(PreparedStatement p,Producto entidad) throws SQLException {
        p.setString(1,entidad.getCodigo());
        p.setString(2,entidad.getNombre());
        p.setString(3,entidad.getDescripcion());
        p.setString(4,entidad.getTipo());
        p.setString(5,entidad.getCategoria());
        p.setString(6,entidad.getUnidadMedida());
        p.setBigDecimal(7,entidad.getPrecioCompra());
        p.setBigDecimal(8,entidad.getPrecioVenta());
        p.setBigDecimal(9,entidad.getStock());
        p.setBigDecimal(10,entidad.getStockMinimo());
        p.setString(11,entidad.getEstado());
    }
    @Override public void guardar(Producto entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO productos(codigo,nombre,descripcion,tipo,categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdProducto(r.getInt(1)); }
        }
    }
    @Override public void actualizar(Producto entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE productos SET codigo=?,nombre=?,descripcion=?,tipo=?,categoria=?,unidad_medida=?,precio_compra=?,precio_venta=?,stock=?,stock_minimo=?,estado=? WHERE id_producto=? AND (tipo=? OR NOT EXISTS(SELECT 1 FROM solped_detalle WHERE id_producto=productos.id_producto))")) {
            parametros(p,entidad); p.setInt(12,entidad.getIdProducto()); p.setString(13,entidad.getTipo());
            if(p.executeUpdate()==0) throw new IllegalArgumentException("El registro ya no existe o su tipo está vinculado a pedidos. Crea otro producto para cambiar de tipo.");
        }
    }
    @Override public void eliminar(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("DELETE FROM productos WHERE id_producto=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
}
