// Autor: Rodriguez Alvarado Andersson Eduardo
package controlador;

import conexion.ConexionBD;
import modelo.Usuario;
import util.Seguridad;
import util.Web;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String accion=Web.texto(request,"accion",20);
            if ("nuevo".equals(accion) || "editar".equals(accion) || "clave".equals(accion)) {
                Usuario u="nuevo".equals(accion) ? new Usuario() : buscarPorId(Web.id(request,"id"));
                if(u==null) throw new IllegalArgumentException("El usuario no existe.");
                if(u.getIdUsuario()==0) { u.setRol("VENDEDOR"); u.setEstado("ACTIVO"); }
                request.setAttribute("registro",u);
                request.getRequestDispatcher("/WEB-INF/vistas/"+("clave".equals(accion)?"usuario_clave.jsp":"usuario_formulario.jsp")).forward(request,response);
            } else {
                String q=Web.texto(request,"q",100);
                request.setAttribute("q",q); request.setAttribute("registros",listar(q));
                request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request,response);
            }
        } catch (SQLException e) {
            log("Error al consultar usuarios",e); request.setAttribute("error","No se pudo cargar la información. Intenta nuevamente.");
            request.getRequestDispatcher("/WEB-INF/vistas/usuarios.jsp").forward(request,response);
        } catch (IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); response.sendRedirect(request.getContextPath()+"/usuarios"); }
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String accion=Web.requerido(request,"accion",20);
            if ("guardar".equals(accion)) {
                Usuario u = new Usuario();
                String id=Web.texto(request,"id",10);
                u.setIdUsuario(id.isEmpty()?0:Web.id(request,"id"));
                u.setNombres(Web.requerido(request,"nombres",100)); u.setApellidos(Web.requerido(request,"apellidos",100));
                u.setDni(Web.requerido(request,"dni",8)); if(!u.getDni().matches("[0-9]{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
                u.setCorreo(Web.requerido(request,"correo",150)); Web.correo(u.getCorreo());
                u.setUsuario(Web.requerido(request,"usuario",50));
                u.setRol(Web.opcion(Web.requerido(request,"rol",10),"ADMIN","VENDEDOR"));
                u.setEstado(Web.opcion(Web.requerido(request,"estado",10),"ACTIVO","BLOQUEADO","INACTIVO"));
                if(u.getIdUsuario()==0) { u.setClaveHash(Seguridad.generarHash(Web.clave(request))); guardar(u); }
                else {
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario") && (!"ACTIVO".equals(u.getEstado()) || !"ADMIN".equals(u.getRol())))
                        throw new IllegalArgumentException("Conserva tu cuenta activa con rol ADMIN para no perder el acceso.");
                    actualizar(u);
                    if (u.getIdUsuario()==(Integer)request.getSession().getAttribute("idUsuario")) {
                        request.getSession().setAttribute("usuario",u.getUsuario()); request.getSession().setAttribute("nombreUsuario",u.getNombres());
                    }
                }
            } else {
                int id=Web.id(request,"id");
                if(buscarPorId(id)==null) throw new IllegalArgumentException("El usuario no existe.");
                switch(accion) {
                    case "eliminar":
                        if(id==(Integer)request.getSession().getAttribute("idUsuario")) throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
                        eliminar(id); break;
                    case "desbloquear": desbloquear(id); break;
                    case "clave": cambiarClave(id,Seguridad.generarHash(Web.clave(request))); break;
                    default: throw new IllegalArgumentException("Acción no válida.");
                }
            }
            Web.mensaje(request,"Operación realizada correctamente.");
        } catch (IllegalArgumentException e) { Web.mensaje(request,e.getMessage()); }
        catch (SQLException e) {
            log("Error al modificar usuario",e);
            Web.mensaje(request,e.getErrorCode()==1062?"El usuario, DNI o correo ya está registrado.":"No se pudo guardar: el registro puede estar relacionado con otros datos.");
        }
        response.sendRedirect(request.getContextPath()+"/usuarios");
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
}
