<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%
request.setAttribute("tipoRegistro","VENDEDOR");
request.setAttribute("tituloRegistro","Solicita ser vendedor");
request.setAttribute("descripcionRegistro","Regístrate para trabajar con el panel de la tienda. No necesitas aprobación previa.");
%>
<%@include file="WEB-INF/vistas/registro_publico.jspf"%>
