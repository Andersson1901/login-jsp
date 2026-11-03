<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%
if (request.getAttribute("catalogoCargado") == null) {
    response.sendRedirect(request.getContextPath() + "/catalogo");
    return;
}
%>
<%@include file="WEB-INF/vistas/publica_cabecera.jspf"%>
<section class="hero-catalogo mb-4 p-4 p-md-5">
<p class="etiqueta-publica">MODAEXPRESS PERÚ</p>
<h1>Tu estilo, todos los días.</h1>
<p class="mb-0">Explora nuestra ropa, insumos y servicios de confección.</p>
</section>
<form class="d-flex flex-wrap gap-2 mb-4" method="get" action="${pageContext.request.contextPath}/catalogo">
<label for="q" class="visually-hidden">Buscar productos</label>
<input id="q" class="form-control buscador-publico" name="q" maxlength="100" placeholder="Busca por nombre, categoría o código" value="<c:out value='${q}'/>">
<button class="btn btn-marino">Buscar</button>
<c:if test="${not empty q}"><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/catalogo">Ver todos</a></c:if>
</form>
<c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
<div class="row g-4">
<c:forEach var="p" items="${productos}"><div class="col-sm-6 col-lg-4">
<article class="card h-100 border-0 shadow-sm producto-publico"><div class="card-body p-4 d-flex flex-column">
<div class="mb-3"><span class="badge categoria-publica"><c:out value="${p.categoria}"/></span></div>
<h2 class="h5"><c:out value="${p.nombre}"/></h2>
<p class="text-muted small"><c:out value="${p.codigo}"/> · <c:out value="${p.tipo}"/></p>
<p><c:out value="${p.descripcion}"/></p>
<div class="mt-auto pt-3"><p class="precio-publico mb-0">S/ <fmt:formatNumber value="${p.precioVenta}" minFractionDigits="2" maxFractionDigits="2"/></p>
<small class="text-muted">Sin IGV · por <c:out value="${p.unidadMedida}"/></small>
<p class="mt-2 mb-0"><c:choose><c:when test="${p.tipo == 'SERVICIO'}">Servicio disponible</c:when><c:when test="${p.stock > 0}">Disponible</c:when><c:otherwise>Agotado</c:otherwise></c:choose></p></div>
</div></article></div></c:forEach>
</div>
<c:if test="${empty productos and empty error}"><div class="card p-4 text-center">No hay productos activos que coincidan con tu búsqueda.</div></c:if>
<p class="text-muted mt-4">Catálogo de consulta. Las ventas se registran desde el panel de la tienda.</p>
<%@include file="WEB-INF/vistas/publica_pie.jspf"%>
