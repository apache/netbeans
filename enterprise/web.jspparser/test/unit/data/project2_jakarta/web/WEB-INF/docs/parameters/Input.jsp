<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>
<%@taglib prefix="c" uri="/WEB-INF/META-INF/c.tld" %>

<fmt:setLocale value="${requestScope.lang}"/>
<fmt:setBundle basename="org.klopp.messages.Bundle" var="bundle" scope="page"/>
<h3><fmt:message key="provide_input" bundle="${bundle}"/></h3>

<form method="POST" action="Output.jsp">
<font size="+1" >
<table>
<tr>
    <td><fmt:message key="type_input" bundle="${bundle}"/>:<td>
    <td><input type="text" size="20" name="input" value=""></td>
</tr>

<%-- We need a tag right here to handle this button for i18n--%>
<tr>
<td><input type="submit" 
           text="#996633"
           value="Submit data">
</td><td></td>
</tr>
</table>
</form>