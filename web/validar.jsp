<%-- 
    Document   : validar
    Created on : 29 set. 2026, 23:14:53
    Author     : ander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
String usuario = request.getParameter("usuario");
String clave = request.getParameter("clave");

if ("admin".equals(usuario) && "1234".equals(clave)) {
    session.setAttribute("usuario", usuario);
    response.sendRedirect("bienvenido.jsp");
} else {
    response.sendRedirect("login.jsp?error=1");
}
%>