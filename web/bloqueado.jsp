<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%
javax.servlet.http.HttpSession anterior = request.getSession(false);
if (anterior != null) anterior.invalidate();
response.setHeader("Cache-Control", "no-store");
%>
<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Acceso restringido | ModaExpress Perú</title>
<style>body{margin:0;min-height:100vh;display:grid;place-items:center;font:16px "Segoe UI",sans-serif;background:#0d1f4d;color:#0d1f4d}.tarjeta{background:white;padding:3rem;border-radius:20px;max-width:480px;margin:20px;text-align:center}h1 span{color:#e0b83c}a{color:#0d1f4d}#cerrar{color:#665116;font-weight:bold}</style></head>
<body><main class="tarjeta"><h1>Moda<span>Express</span> Perú</h1>
<h2>No se pudo iniciar sesión</h2><p>El acceso está restringido. Solicita ayuda al administrador.</p>
<p>La sesión se ha cerrado.</p><p id="cerrar" hidden>Ya puedes cerrar esta pestaña</p>
<a href="login.jsp">Volver al inicio</a></main>
<script>setTimeout(function(){window.close();document.getElementById("cerrar").hidden=false;},3000);</script></body></html>
