// Autor: Rodriguez Alvarado Andersson Eduardo
package util;

import conexion.ConexionBD;
import modelo.Usuario;
import modelo.ResultadoAcceso;
import java.sql.*;

public class Autenticacion {
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
