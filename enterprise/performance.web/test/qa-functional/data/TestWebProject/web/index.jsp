<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib prefix="mytag" uri="/WEB-INF/MyTLD" %>
<html>
<head><title>JSP Page</title></head>
<body>
<jsp:useBean id="bean" scope="session" class="test.TestBean" />


<mytag:HelloWorld/>

<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

</body>
</html>
