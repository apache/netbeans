<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>
<%@taglib prefix="c" uri="/WEB-INF/META-INF/c.tld"%>
<%@taglib prefix="tf" tagdir="/WEB-INF/tags/"%>

<html>
<head><title>JSP 2.0 Sample Application</title></head>
<body text="#996633" link="#cc6600" vlink="#993300" alink="#000000">
<font face="Helvetica, Arial, sans-serif">

<fmt:setLocale value="${requestScope.lang}"/>
<fmt:setBundle basename="org.klopp.messages.Bundle" var="bundle" scope="page"/>


<TABLE border="0">
<tr>
<table border="0">
<tr>
<td width="80" height="100">&nbsp;</td>
<td width="500" 
    height="100" 
    text="#996633" 
    bgcolor="#ffff99"
    valign="center"
    halign="center">

<%@include file="header.jspf"%>

</td>
</tr><tr>
<td width="90" 
    height="300" 
    text="#996633" 
    bgcolor="#ffff99"
    valign="top">
<tf:linklist>
<jsp:attribute name="separator">
<br>
</jsp:attribute>
</tf:linklist>

</td>
<td width="500" 
    height="300" 
    valign="top"
    cellpadding="15"
    cellspacing="15">
<c:set var="page" value="${requestScope.page}"/>
<%-- <jsp:include page="<%=(String)request.getAttribute(\"page\")%>" flush="true"/> --%>
<%-- <jsp:include page="${requestScope.page}"/> --%>
<jsp:include page="${page}"/>
</td>
</tr>
</table>
</tr>
<tr>
<td width="580" 
    height="50" 
    text="#996633" 
    bgcolor="#ffff99"
    valign="top">
<tf:linklist>
<jsp:attribute name="separator">|</jsp:attribute></tf:linklist>
</td></tr>
</font></TABLE>

</body>
</html>