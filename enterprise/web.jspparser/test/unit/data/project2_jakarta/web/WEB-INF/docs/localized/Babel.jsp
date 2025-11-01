<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>

<fmt:setLocale value="${requestScope.lang}"/>
<fmt:setBundle basename="org.klopp.messages.Bundle" var="bundle" scope="page"/>
 <h3><fmt:message key="title" bundle="${bundle}"/></h3>
<p><fmt:message key="message" bundle="${bundle}"/></p>
</fmt:bundle>
</p>
</body>
</html>
