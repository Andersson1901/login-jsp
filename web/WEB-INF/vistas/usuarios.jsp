<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%@include file="cabecera.jspf"%>
<div class="d-flex justify-content-between align-items-center mb-3"><h1>Usuarios</h1><a class="btn btn-dorado" href="usuarios?accion=nuevo">Nuevo usuario</a></div>
<form method="get" action="usuarios" class="d-flex gap-2 mb-3"><input class="form-control" name="q" maxlength="100" aria-label="Buscar usuarios" placeholder="Usuario, nombre o DNI" value="<c:out value='${q}'/>"><button class="btn btn-primary">Buscar</button></form>
<div class="card"><div class="table-responsive"><table class="table align-middle mb-0"><thead><tr><th>Usuario</th><th>Nombre</th><th>DNI / Correo</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>
<c:forEach var="u" items="${registros}"><tr><td><c:out value="${u.usuario}"/></td><td><c:out value="${u.nombres}"/> <c:out value="${u.apellidos}"/></td><td><c:out value="${u.dni}"/><br><small><c:out value="${u.correo}"/></small></td><td><c:out value="${u.rol}"/></td><td><span class="estado"><c:out value="${u.estado}"/></span><br><small>Intentos: <c:out value="${u.intentosFallidos}"/></small></td>
<td><div class="acciones"><a class="btn btn-sm btn-outline-primary" href="usuarios?accion=editar&amp;id=${u.idUsuario}">Editar</a><a class="btn btn-sm btn-outline-secondary" href="usuarios?accion=clave&amp;id=${u.idUsuario}">Clave</a>
<form method="post" action="usuarios"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="id" value="${u.idUsuario}"><button class="btn btn-sm btn-outline-success" name="accion" value="desbloquear">Desbloquear</button></form>
<form method="post" action="usuarios" data-confirmar="¿Eliminar este usuario? Esta acción no se puede deshacer."><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="id" value="${u.idUsuario}"><button class="btn btn-sm btn-outline-danger" name="accion" value="eliminar">Eliminar</button></form></div></td></tr></c:forEach>
<c:if test="${empty registros}"><tr><td colspan="6" class="tabla-vacia">No hay usuarios para mostrar.</td></tr></c:if></tbody></table></div></div>
<%@include file="pie.jspf"%>
