<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="/WEB-INF/META-INF/c.tld"%>
<%@taglib prefix="tf" tagdir="/WEB-INF/tags/"%>

<html>
<body>

<%-- incorrect, but should still be partially parsed --%>
<fmt:setLocaale value="${requestScope.lang}"/>
<fmt:setBundle basename="org.klopp.messages.Bundle" var="bundle" scope="page"/>

</body>
</html>