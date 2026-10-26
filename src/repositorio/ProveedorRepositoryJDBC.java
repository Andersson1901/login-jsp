// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Proveedor;
import java.sql.*;
import java.util.*;

public class ProveedorRepositoryJDBC implements ProveedorRepository {
    private Proveedor mapear(ResultSet r) throws SQLException {
        Proveedor entidad = new Proveedor();
        entidad.setIdProveedor(r.getInt("id_proveedor"));
        entidad.setRuc(r.getString("ruc"));
        entidad.setRazonSocial(r.getString("razon_social"));
        entidad.setContacto(r.getString("contacto"));
        entidad.setTelefono(r.getString("telefono"));
        entidad.setCorreo(r.getString("correo"));
        entidad.setDireccion(r.getString("direccion"));
        entidad.setEstado(r.getString("estado"));
        return entidad;
    }
    @Override public List<Proveedor> listar() throws SQLException { return listar(""); }
    @Override public List<Proveedor> listar(String busqueda) throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM proveedores WHERE ruc LIKE ? OR razon_social LIKE ? ORDER BY id_proveedor")) {
            for(int i=1;i<=2;i++) p.setString(i,"%"+busqueda+"%");
            try(ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
    @Override public Proveedor buscarPorId(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM proveedores WHERE id_proveedor=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    private void parametros(PreparedStatement p,Proveedor entidad) throws SQLException {
        p.setString(1,entidad.getRuc());
        p.setString(2,entidad.getRazonSocial());
        p.setString(3,entidad.getContacto());
        p.setString(4,entidad.getTelefono());
        p.setString(5,entidad.getCorreo());
        p.setString(6,entidad.getDireccion());
        p.setString(7,entidad.getEstado());
    }
    @Override public void guardar(Proveedor entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO proveedores(ruc,razon_social,contacto,telefono,correo,direccion,estado) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdProveedor(r.getInt(1)); }
        }
    }
    @Override public void actualizar(Proveedor entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE proveedores SET ruc=?,razon_social=?,contacto=?,telefono=?,correo=?,direccion=?,estado=? WHERE id_proveedor=?")) {
            parametros(p,entidad); p.setInt(8,entidad.getIdProveedor());
            if(p.executeUpdate()==0) throw new IllegalArgumentException("El registro ya no existe.");
        }
    }
    @Override public void eliminar(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("DELETE FROM proveedores WHERE id_proveedor=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
}
