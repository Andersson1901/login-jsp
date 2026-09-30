<%-- 
    Document   : logout
    Created on : 29 set. 2026, 23:15:15
    Author     : ander
--%>

<%
session.invalidate();
response.sendRedirect("login.jsp");
%>
