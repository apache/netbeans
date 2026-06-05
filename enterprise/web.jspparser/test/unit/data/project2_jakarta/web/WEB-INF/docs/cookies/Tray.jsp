<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@taglib prefix="c" uri="/WEB-INF/META-INF/c.tld"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>

<h3>Incoming cookies</h3>
<table border="1">
<tr><th halign="center">#</th><th align="left">Name</th><th align="left">Value</th></tr>

<c:set var="i" value="0"/>
<c:forEach var="ck" items="${requestScope.cookies}">
<c:set var="i" value="${i+1}"/>
  <tr><td>${i}</td>
      <td>${ck.name}</td>
      <td>${ck.value}</td></tr>
</c:forEach>

</table>
</body>
</html>
