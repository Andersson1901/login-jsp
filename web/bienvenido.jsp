<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%@include file="WEB-INF/vistas/cabecera.jspf"%>
<div class="d-flex justify-content-between align-items-center mb-4"><div><p class="text-muted mb-1">Panel principal</p><h1>Hola, <c:out value="${sessionScope.nombreUsuario}"/></h1><p>Organiza tu tienda desde un solo lugar.</p></div><span class="badge text-bg-light">ModaExpress Perú</span></div>
<jsp:include page="/resumen"/>
<div class="card mt-4"><div class="card-body p-4"><h2 class="h5">Del pedido a la entrega</h2><p class="mb-0">SOLPED → Venta → Factura o boleta → Guía de remisión</p></div></div>
<%@include file="WEB-INF/vistas/pie.jspf"%>
