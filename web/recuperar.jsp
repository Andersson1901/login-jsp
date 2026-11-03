<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page pageEncoding="UTF-8"%>
<%@include file="WEB-INF/vistas/publica_cabecera.jspf"%>
<div class="formulario-publico mx-auto">
<p class="etiqueta-publica">ACCESO A TU CUENTA</p><h1 class="h2">¿Olvidaste tu contraseña?</h1>
<p class="text-muted">Introduce el correo de tu cuenta para probar la recuperación.</p>
<div class="card border-0 shadow-sm"><div class="card-body p-4">
<div class="alert alert-info">Esta es una simulación académica: no se envía ningún correo ni se cambia tu contraseña.</div>
<c:if test="${param.enviado == '1'}"><div class="alert alert-success" role="status">Solicitud procesada. Si el correo corresponde a una cuenta activa, se ha simulado el envío de recuperación. El token tiene una vigencia de 15 minutos.</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/recuperar">
<input type="hidden" name="csrfPublico" value="<c:out value='${csrfPublico}'/>">
<label for="correo" class="form-label">Correo electrónico</label>
<input id="correo" class="form-control" name="correo" type="email" maxlength="150" autocomplete="email" required>
<button class="btn btn-dorado w-100 mt-4">Simular envío</button>
</form><p class="mt-3 mb-0"><a href="${pageContext.request.contextPath}/login.jsp">Volver al inicio de sesión</a></p>
</div></div></div>
<%@include file="WEB-INF/vistas/publica_pie.jspf"%>
