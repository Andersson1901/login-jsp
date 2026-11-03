<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%
request.setAttribute("tipoRegistro","TIENDA");
request.setAttribute("tituloRegistro","Registra tu tienda");
request.setAttribute("descripcionRegistro","Crea tu cuenta como dueño e indica el nombre de tu negocio.");
%>
<%@include file="WEB-INF/vistas/registro_publico.jspf"%>
