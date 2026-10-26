<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="conexion.ConexionBD,java.sql.*"%>
<%
request.setCharacterEncoding("UTF-8");
if (!"POST".equals(request.getMethod())) {
    response.sendRedirect("login.jsp");
    return;
}
String usuario = request.getParameter("usuario");
String clave = request.getParameter("clave");
boolean valido = false;
try (Connection conexion = new ConexionBD().getConexion();
     PreparedStatement consulta = conexion.prepareStatement("SELECT usuario FROM usuarios WHERE usuario = ? AND clave = ?")) {
    consulta.setString(1, usuario);
    consulta.setString(2, clave);
    try (ResultSet resultado = consulta.executeQuery()) {
        if (resultado.next()) {
            usuario = resultado.getString("usuario");
            valido = true;
        }
    }
} catch (SQLException e) {
    application.log("No se pudo validar el acceso", e);
}
if (valido) {
    request.changeSessionId();
    session.setAttribute("usuario", usuario);
    response.sendRedirect("bienvenido.jsp");
} else {
    response.sendRedirect("login.jsp?error=1");
}
%>
