<%-- Autor: Rodriguez Alvarado Andersson Eduardo --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
if ("POST".equals(request.getMethod())) session.invalidate();
response.sendRedirect("login.jsp");
%>
