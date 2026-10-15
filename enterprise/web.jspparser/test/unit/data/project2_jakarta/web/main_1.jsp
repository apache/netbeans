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

<tf:linklist>
<jsp:attribute name="separator">
<br>
</jsp:attribute>
<jsp:body>
blah
</jsp:body>
</tf:linklist>


</body>
</html>