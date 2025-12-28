<%@taglib prefix="c" uri="/WEB-INF/META-INF/c.tld" %>

<%! String page = "myotherpage.jsp"; %>

<%--

<jsp:include page="<%=page%>">
<jsp:param name="hello" value="ana"/>
</jsp:include>


<jsp:plugin code="org.ana.Something" type="applet" codebase="/applets">
<jsp:params>
<jsp:param name="hello" value="ana"/>
</jsp:params>
<jsp:fallback>
Some text to show instead
</jsp:fallback>
</jsp:plugin> 
--%>

<c:set var="size" value="2"/>
<c:set var="level" value="3"/>
<c:set var="text" value="This is the header"/>
<jsp:element name="H${level}">
<jsp:attribute name="size">${size}</jsp:attribute> 
<jsp:body>${text}</jsp:body>
</jsp:element>
