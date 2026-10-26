// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import conexion.ConexionBD;
import modelo.Usuario;
import modelo.ResultadoAcceso;
import util.Seguridad;
import java.sql.*;
import java.util.*;

public class UsuarioRepositoryJDBC implements UsuarioRepository {
    @Override public List<Usuario> listar() throws SQLException { return listar(""); }

    @Override public Usuario buscarPorUsuario(String usuario) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM usuarios WHERE usuario=?")) {
            p.setString(1,usuario); try(ResultSet r=p.executeQuery()) { return r.next()?mapear(r):null; }
        }
    }
    private Usuario mapear(ResultSet r) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(r.getInt("id_usuario")); u.setNombres(r.getString("nombres"));
        u.setApellidos(r.getString("apellidos")); u.setDni(r.getString("dni")); u.setCorreo(r.getString("correo"));
        u.setUsuario(r.getString("usuario")); u.setRol(r.getString("rol")); u.setEstado(r.getString("estado"));
        u.setIntentosFallidos(r.getInt("intentos_fallidos")); u.setFechaCreacion(r.getTimestamp("fecha_creacion"));
        u.setUltimoAcceso(r.getTimestamp("ultimo_acceso"));
        return u;
    }

    public List<Usuario> listar(String busqueda) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = ConexionBD.getInstancia().getConexion();
             PreparedStatement p = c.prepareStatement("SELECT * FROM usuarios WHERE usuario LIKE ? OR nombres LIKE ? OR apellidos LIKE ? OR dni LIKE ? ORDER BY id_usuario")) {
            for (int i=1; i<=4; i++) p.setString(i, "%" + busqueda + "%");
            try (ResultSet r=p.executeQuery()) { while (r.next()) lista.add(mapear(r)); }
        }
        return lista;
    }
    public Usuario buscarPorId(Integer id) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("SELECT * FROM usuarios WHERE id_usuario=?")) {
            p.setInt(1,id); try (ResultSet r=p.executeQuery()) { return r.next() ? mapear(r) : null; }
        }
    }
    private void parametros(PreparedStatement p, Usuario u) throws SQLException {
        p.setString(1,u.getNombres()); p.setString(2,u.getApellidos()); p.setString(3,u.getDni());
        p.setString(4,u.getCorreo()); p.setString(5,u.getUsuario()); p.setString(6,u.getRol()); p.setString(7,u.getEstado());
    }
    public void guardar(Usuario u) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("INSERT INTO usuarios(nombres,apellidos,dni,correo,usuario,rol,estado,clave_hash) VALUES(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            parametros(p,u); p.setString(8,u.getClaveHash()); p.executeUpdate();
            try (ResultSet r=p.getGeneratedKeys()) { if(r.next()) u.setIdUsuario(r.getInt(1)); }
        }
    }
    public void actualizar(Usuario u) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE usuarios SET nombres=?,apellidos=?,dni=?,correo=?,usuario=?,rol=?,estado=?,intentos_fallidos=CASE WHEN ?='ACTIVO' THEN 0 ELSE intentos_fallidos END WHERE id_usuario=?")) {
            parametros(p,u); p.setString(8,u.getEstado()); p.setInt(9,u.getIdUsuario());
            if(p.executeUpdate()==0) throw new IllegalArgumentException("El usuario ya no existe.");
        }
    }
    public void eliminar(Integer id) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("DELETE FROM usuarios WHERE id_usuario=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
    public void desbloquear(Integer id) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE usuarios SET estado='ACTIVO',intentos_fallidos=0 WHERE id_usuario=?")) {
            p.setInt(1,id); p.executeUpdate();
        }
    }
    public void cambiarClave(Integer id, String hash) throws SQLException {
        try (Connection c=ConexionBD.getInstancia().getConexion(); PreparedStatement p=c.prepareStatement("UPDATE usuarios SET clave_hash=? WHERE id_usuario=?")) {
            p.setString(1,hash); p.setInt(2,id); p.executeUpdate();
        }
    }
    private static final String HASH_SIMULADO = Seguridad.generarHash("ComparacionDeTiempo");

    public ResultadoAcceso autenticar(String nombre, String clave) throws SQLException {
        try (Connection conexion = ConexionBD.getInstancia().getConexion()) {
            conexion.setAutoCommit(false);
            try {
                Usuario usuario = null;
                try (PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE usuario=? FOR UPDATE")) {
                    consulta.setString(1, nombre);
                    try (ResultSet r = consulta.executeQuery()) {
                        if (r.next()) {
                            usuario = new Usuario();
                            usuario.setIdUsuario(r.getInt("id_usuario"));
                            usuario.setUsuario(r.getString("usuario"));
                            usuario.setNombres(r.getString("nombres"));
                            usuario.setRol(r.getString("rol"));
                            usuario.setEstado(r.getString("estado"));
                            usuario.setClaveHash(r.getString("clave_hash"));
                            usuario.setIntentosFallidos(r.getInt("intentos_fallidos"));
                        }
                    }
                }
                boolean coincide = Seguridad.verificar(clave, usuario == null ? HASH_SIMULADO : usuario.getClaveHash());
                if (usuario == null) {
                    conexion.commit();
                    return new ResultadoAcceso(null, false);
                }
                if (!"ACTIVO".equals(usuario.getEstado())) {
                    conexion.commit();
                    return new ResultadoAcceso(null, true);
                }
                if (coincide) {
                    try (PreparedStatement cambio = conexion.prepareStatement("UPDATE usuarios SET intentos_fallidos=0, ultimo_acceso=CURRENT_TIMESTAMP WHERE id_usuario=?")) {
                        cambio.setInt(1, usuario.getIdUsuario());
                        cambio.executeUpdate();
                    }
                    conexion.commit();
                    usuario.setClaveHash(null);
                    return new ResultadoAcceso(usuario, false);
                }
                int intentos = usuario.getIntentosFallidos() + 1;
                try (PreparedStatement cambio = conexion.prepareStatement("UPDATE usuarios SET intentos_fallidos=?, estado=? WHERE id_usuario=?")) {
                    cambio.setInt(1, intentos);
                    cambio.setString(2, intentos >= 3 ? "BLOQUEADO" : "ACTIVO");
                    cambio.setInt(3, usuario.getIdUsuario());
                    cambio.executeUpdate();
                }
                conexion.commit();
                return new ResultadoAcceso(null, intentos >= 3);
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }
}
