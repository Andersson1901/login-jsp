<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="repositorio.UsuarioRepository,repositorio.Repositorios,modelo.ResultadoAcceso,modelo.Usuario,java.sql.SQLException"%>
<%
request.setCharacterEncoding("UTF-8");
if (!"POST".equals(request.getMethod())) {
    response.sendRedirect("login.jsp");
    return;
}
String nombre = request.getParameter("usuario");
String clave = request.getParameter("clave");
if (nombre == null || nombre.length() > 50 || clave == null || clave.length() > 128) {
    response.sendRedirect("login.jsp?error=1");
    return;
}
try {
    UsuarioRepository usuarios = Repositorios.usuarios();
    ResultadoAcceso resultado = usuarios.autenticar(nombre.trim(), clave);
    Usuario usuario = resultado.getUsuario();
    if (usuario != null) {
        session.invalidate();
        javax.servlet.http.HttpSession nueva = request.getSession(true);
        nueva.setAttribute("usuario", usuario.getUsuario());
        nueva.setAttribute("idUsuario", usuario.getIdUsuario());
        nueva.setAttribute("nombreUsuario", usuario.getNombres());
        nueva.setAttribute("rol", usuario.getRol());
        nueva.setAttribute("csrf", java.util.UUID.randomUUID().toString());
        nueva.setMaxInactiveInterval(1800);
        response.sendRedirect("CLIENTE".equals(usuario.getRol()) ? "catalogo" : "bienvenido.jsp");
    } else {
        int intentos;
        synchronized (session) {
            Integer anteriores = (Integer) session.getAttribute("intentosAcceso");
            intentos = anteriores == null ? 1 : anteriores + 1;
            session.setAttribute("intentosAcceso", intentos);
        }
        if (resultado.isBloqueado() || intentos >= 3) {
            session.invalidate();
            response.sendRedirect("bloqueado.jsp");
        } else {
            response.sendRedirect("login.jsp?error=1");
        }
    }
} catch (SQLException | RuntimeException e) {
    application.log("No se pudo validar el acceso", e);
    response.sendRedirect("login.jsp?error=1");
}
%>
