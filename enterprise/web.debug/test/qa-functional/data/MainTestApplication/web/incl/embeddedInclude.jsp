<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<h3>incl/embeddedInclude.jsp</h3>

<b>Using include directive inside included JSP:</b><br>
<%@include file="simpleInclude.jsp"%>

<b>Using include directive inside included JSP with relative path:</b><br>
<%@include file="../incl/simpleInclude.jsp"%>