<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose><c:when test="${errorResumen}"><div class="alert alert-warning">No se pudo cargar el resumen. Intenta nuevamente.</div></c:when><c:otherwise>
<div class="row g-3"><c:forEach var="dato" items="${resumen}"><div class="col-sm-6 col-xl-3"><div class="card resumen h-100"><div class="card-body"><p class="text-capitalize"><c:out value="${dato.key}"/></p><strong class="display-6"><c:out value="${dato.value}"/></strong></div></div></div></c:forEach></div>
</c:otherwise></c:choose>
