<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%
    application.getClass();
%>
<%-- completion for JSP elements --%>
<%--CC
<jsp:|
jsp:text
<jsp:text
--%>

<%-- completion for including JSP page --%>
<%--CC
<jsp:include page="testJS|"
testJSPElements.jsp
<jsp:include page="testJSPElements.jsp"
--%>

<%-- completion for Java beans --%>
<%--CC
<jsp:useBean id="myBean" scope="request" class="java.lang.S|"
String
<jsp:useBean id="myBean" scope="request" class="java.lang.String"
--%>

<jsp:useBean id="myBean" scope="request" class="java.lang.String"/>
<% myBean.charAt(0); %>

<%-- completion for beans in scriptlets  #58437 --%>
<%--
<%  |  %>
String myBean
<% myBean %>
--%>

<%-- Java completion for beans in scriptlets #58437 --%>
<%--
<% myBean.c| %>
char charAt (int index )
<% myBean.charAt(index) %>
--%>

<html>
    <head><title>JSP Elements Code Completion Page</title></head>
    <body>
        <h1>JSP Elements Code Completion Page</h1>
    </body>
</html>
