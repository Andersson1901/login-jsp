// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;
import conexion.ConexionBD;
import modelo.Cliente;
import java.sql.*;
import java.util.*;

public class ClienteRepositoryJDBC implements ClienteRepository {
    private Cliente mapear(ResultSet r) throws SQLException {
        Cliente entidad = new Cliente();
        entidad.setIdCliente(r.getInt("id_cliente"));
        entidad.setTipoDocumento(r.getString("tipo_documento"));
        entidad.setNroDocumento(r.getString("nro_documento"));
        entidad.setNombresRazonSocial(r.getString("nombres_razon_social"));
        entidad.setDireccion(r.getString("direccion"));
        entidad.setTelefono(r.getString("telefono"));
        entidad.setCorreo(r.getString("correo"));
        entidad.setEstado(r.getString("estado"));
        return entidad;
    }
    @Override public List<Cliente> listar() throws SQLException { return listar(""); }
    @Override public List<Cliente> listar(String busqueda) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM clientes WHERE nro_documento LIKE ? OR nombres_razon_social LIKE ? ORDER BY id_cliente")) {
            for(int i=1;i<=2;i++) p.setString(i,"%"+busqueda+"%");
            try(ResultSet r=p.executeQuery()) { while(r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
    @Override public Cliente buscarPorId(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM clientes WHERE id_cliente=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    private void parametros(PreparedStatement p,Cliente entidad) throws SQLException {
        p.setString(1,entidad.getTipoDocumento());
        p.setString(2,entidad.getNroDocumento());
        p.setString(3,entidad.getNombresRazonSocial());
        p.setString(4,entidad.getDireccion());
        p.setString(5,entidad.getTelefono());
        p.setString(6,entidad.getCorreo());
        p.setString(7,entidad.getEstado());
    }
    @Override public void guardar(Cliente entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO clientes(tipo_documento,nro_documento,nombres_razon_social,direccion,telefono,correo,estado) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,entidad); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) entidad.setIdCliente(r.getInt(1)); }
        }
    }
    @Override public void actualizar(Cliente entidad) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE clientes SET tipo_documento=?,nro_documento=?,nombres_razon_social=?,direccion=?,telefono=?,correo=?,estado=? WHERE id_cliente=?")) {
            parametros(p,entidad); p.setInt(8,entidad.getIdCliente());
            if(p.executeUpdate()==0) throw new IllegalArgumentException("El registro ya no existe.");
        }
    }
    @Override public void eliminar(Integer id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("DELETE FROM clientes WHERE id_cliente=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
}
