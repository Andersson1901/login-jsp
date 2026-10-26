<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose><c:when test="${errorResumen}"><div class="alert alert-warning">No se pudo cargar el resumen. Intenta nuevamente.</div></c:when><c:otherwise>
<div class="row g-3"><div class="col-md-4"><div class="card resumen h-100"><div class="card-body"><p>Usuarios registrados</p><strong class="display-6"><c:out value="${usuariosTotal}"/></strong></div></div></div>
<div class="col-md-4"><div class="card resumen h-100"><div class="card-body"><p>Catálogo de ropa</p><h2 class="h5">Clientes, proveedores y productos</h2></div></div></div>
<div class="col-md-4"><div class="card resumen h-100"><div class="card-body"><p>Operación comercial</p><h2 class="h5">Pedidos y ventas</h2></div></div></div></div>
</c:otherwise></c:choose>
