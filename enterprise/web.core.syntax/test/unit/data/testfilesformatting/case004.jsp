<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
long s;
s =
System.currentTimeMillis();
%>
<html>
<body>
<jsp:useBean id="foo" class="jsp2.examples.FooBean">
Bean created!  Setting foo.bar...<br>
<%
s =
System.currentTimeMillis();
%>
</jsp:useBean>
</body>
</html>
