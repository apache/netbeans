<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>

<h3>Made a cookie for you...</h3> 

<p>Cookie name = ${requestScope.cookie.name}, 
   value = ${requestScope.cookie.value} </p>


<form method="get" action="Tray.jsp">
<input type="submit" value="Cookie tray">
</form>

