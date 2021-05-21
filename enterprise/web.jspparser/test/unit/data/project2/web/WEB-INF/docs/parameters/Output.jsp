<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>
<%@ taglib prefix="c" uri="/WEB-INF/META-INF/c.tld" %>

<fmt:setLocale value="${requestScope.lang}"/>
<fmt:setBundle basename="org.klopp.messages.Bundle" var="bundle" scope="page"/>
<h3><fmt:message key="display_input" bundle="${bundle}"/></h3><p>
<fmt:message key="datareceived" bundle="${bundle}"/>: 
<c:out value="${param.input}"/>
</p>
</body>
</html>
