<%-- 
    Document   : login
    Created on : 29 set. 2026, 23:14:18
    Author     : ander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
</head>
<body>
    <h2>Iniciar sesión</h2>
    <form action="validar.jsp" method="post">
        Usuario: <input type="text" name="usuario" required><br><br>
        Contraseña: <input type="password" name="clave" required><br><br>
        <button type="submit">Ingresar</button>
    </form>
    <% if (request.getParameter("error") != null) { %>
        <p style="color:red">Usuario o contraseña incorrectos</p>
    <% } %>
</body>
</html>