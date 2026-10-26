<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
if ("POST".equals(request.getMethod())) session.invalidate();
response.sendRedirect("login.jsp");
%>
