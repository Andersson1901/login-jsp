<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@tag body-content="empty" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="base" required="true"%>
<%@attribute name="prefijo"%>
<%-- Aviso de búsqueda y selector del límite de filas traídas de la base; cambiar el límite vuelve a la página 1. --%>
<c:set var="pl" value="${prefijo}limite"/><c:set var="pp" value="${prefijo}pagina"/>
<c:if test="${not empty avisoBusqueda}"><div class="alert alert-warning py-2" role="alert"><c:out value="${avisoBusqueda}"/></div></c:if>
<form method="get" action="${base}" class="d-flex justify-content-end align-items-center gap-2 mb-2">
<c:forEach var="p" items="${param}"><c:if test="${p.key != pl and p.key != pp}"><input type="hidden" name="<c:out value='${p.key}'/>" value="<c:out value='${p.value}'/>"></c:if></c:forEach>
<label class="form-label mb-0" for="${pl}">Filas traídas de la BD</label>
<select class="form-select form-select-sm w-auto" id="${pl}" name="${pl}" data-autoenviar><c:forEach var="n" items="50,100,500,1000"><option value="${n}"${(empty param[pl] ? '100' : param[pl]) == n ? ' selected' : ''}>${n}</option></c:forEach></select>
<noscript><button class="btn btn-sm btn-outline-primary">Aplicar</button></noscript>
</form>
