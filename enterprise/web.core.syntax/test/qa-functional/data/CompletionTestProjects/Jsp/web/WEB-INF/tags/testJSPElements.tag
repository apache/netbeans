<%@tag pageEncoding="UTF-8"%>
<%
    application.getClass();
%>
<%-- completion for JSP elements --%>
<%--CC
<jsp:|
jsp:text
<jsp:text
--%>

<%-- completion for including TAG file --%>
<%--CC
<jsp:include page="testJSPElements|"
testJSPElements.tag
<jsp:include page="testJSPElements.tag"
--%>

<%-- completion for Java beans --%>
<%--CC
<jsp:useBean id="myBean" scope="request" class="java.lang.S|"
String
<jsp:useBean id="myBean" scope="request" class="java.lang.String"
--%>

<jsp:useBean id="myBean" scope="request" class="java.lang.Byte"/>
<%-- completion for beans in scriptlets  #58437 --%>
<% myBean.byteValue(); %>

<%--
<%  |   %>
Byte myBean
<% myBean %>
--%>

<%-- Java completion for beans in scriptlets #58437 --%>
<%--
<% myBean.| %>
int SIZE
<% myBean.SIZE %>
--%>


<html>
    <head><title>TAG Elements Code Completion Page</title></head>
    <body>
        <h1>TAG Elements Code Completion Page</h1>
    </body>
</html>
