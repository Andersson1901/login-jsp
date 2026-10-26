<%-- 
    Document   : bienvenido
    Created on : 29 set. 2026, 23:15:05
    Author     : ander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="util.Html"%>
<% String usuario = (String) session.getAttribute("usuario"); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bienvenido</title>
</head>
<body>
    <h2>Bienvenido, <%= Html.escapar(usuario) %></h2>
    <a href="logout.jsp">Cerrar sesión</a>
</body>
</html>
