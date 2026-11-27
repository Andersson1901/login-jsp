<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@tag body-content="empty" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="pagina" required="true" type="java.lang.Object"%>
<%@attribute name="base" required="true"%>
<%@attribute name="prefijo"%>
<%-- Pie de la tabla: "Página X de Y" y enlaces de página. Cada enlace conserva la búsqueda, el orden y el límite. --%>
<c:set var="pp" value="${prefijo}pagina"/>
<c:if test="${pagina.total > 0}">
<div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-3">
<small class="text-muted">Página ${pagina.pagina} de ${pagina.paginas} · filas ${pagina.desde} a ${pagina.hasta} de ${pagina.total} traídas<c:if test="${pagina.total == pagina.limite}"> (tope alcanzado; afina la búsqueda para ver otras)</c:if></small>
<c:if test="${pagina.paginas > 1}"><nav aria-label="Páginas de la tabla"><ul class="pagination pagination-sm mb-0">
<c:choose><c:when test="${pagina.pagina > 1}"><c:url var="enlace" value="${base}"><c:forEach var="p" items="${param}"><c:if test="${p.key != pp}"><c:param name="${p.key}" value="${p.value}"/></c:if></c:forEach><c:param name="${pp}" value="${pagina.pagina - 1}"/></c:url><li class="page-item"><a class="page-link" href="<c:out value='${enlace}'/>">«</a></li></c:when><c:otherwise><li class="page-item disabled"><span class="page-link">«</span></li></c:otherwise></c:choose>
<c:forEach var="n" begin="1" end="${pagina.paginas}"><c:if test="${n == 1 or n == pagina.paginas or (n >= pagina.pagina - 2 and n <= pagina.pagina + 2)}">
<c:if test="${(n == pagina.pagina - 2 and n > 2) or (n == pagina.paginas and n > pagina.pagina + 3)}"><li class="page-item disabled"><span class="page-link">…</span></li></c:if>
<c:url var="enlace" value="${base}"><c:forEach var="p" items="${param}"><c:if test="${p.key != pp}"><c:param name="${p.key}" value="${p.value}"/></c:if></c:forEach><c:param name="${pp}" value="${n}"/></c:url>
<li class="page-item${n == pagina.pagina ? ' active' : ''}"><a class="page-link" href="<c:out value='${enlace}'/>"${n == pagina.pagina ? ' aria-current="page"' : ''}>${n}</a></li></c:if></c:forEach>
<c:choose><c:when test="${pagina.pagina < pagina.paginas}"><c:url var="enlace" value="${base}"><c:forEach var="p" items="${param}"><c:if test="${p.key != pp}"><c:param name="${p.key}" value="${p.value}"/></c:if></c:forEach><c:param name="${pp}" value="${pagina.pagina + 1}"/></c:url><li class="page-item"><a class="page-link" href="<c:out value='${enlace}'/>">»</a></li></c:when><c:otherwise><li class="page-item disabled"><span class="page-link">»</span></li></c:otherwise></c:choose>
</ul></nav></c:if>
</div></c:if>
