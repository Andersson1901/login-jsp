<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%
request.setAttribute("tipoRegistro","COMPRADOR");
request.setAttribute("tituloRegistro","Crea tu cuenta de comprador");
request.setAttribute("descripcionRegistro","Descubre los productos y servicios de nuestra tienda.");
%>
<%@include file="WEB-INF/vistas/registro_publico.jspf"%>
