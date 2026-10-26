// Autor: Rodriguez Alvarado Andersson Eduardo
package repositorio;

import modelo.Usuario;
import modelo.ResultadoAcceso;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioRepository extends Repository<Usuario, Integer> {
    List<Usuario> listar(String busqueda) throws SQLException;
    Usuario buscarPorUsuario(String usuario) throws SQLException;
    void desbloquear(Integer id) throws SQLException;
    void cambiarClave(Integer id, String hash) throws SQLException;
    ResultadoAcceso autenticar(String nombre, String clave) throws SQLException;
}
