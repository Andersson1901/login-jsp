<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@tag body-content="empty" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="base" required="true"%>
<%@attribute name="columna" required="true"%>
<%@attribute name="titulo" required="true"%>
<%@attribute name="prefijo"%>
<%-- Encabezado clicable: el primer clic ordena de forma descendente y otro clic sobre la misma columna alterna a ascendente. --%>
<c:set var="po" value="${prefijo}orden"/><c:set var="pd" value="${prefijo}dir"/><c:set var="pp" value="${prefijo}pagina"/>
<c:set var="activa" value="${param[po] == columna}"/><c:set var="descendente" value="${param[pd] != 'asc'}"/>
<c:url var="enlace" value="${base}"><c:forEach var="p" items="${param}"><c:if test="${p.key != po and p.key != pd and p.key != pp}"><c:param name="${p.key}" value="${p.value}"/></c:if></c:forEach><c:param name="${po}" value="${columna}"/><c:param name="${pd}" value="${activa and descendente ? 'asc' : 'desc'}"/></c:url>
<th aria-sort="${activa ? (descendente ? 'descending' : 'ascending') : 'none'}"><a class="orden-columna" href="<c:out value='${enlace}'/>"><c:out value="${titulo}"/><c:if test="${activa}"> ${descendente ? '▼' : '▲'}</c:if></a></th>
