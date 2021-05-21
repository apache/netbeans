<%@page contentType="text/html"%>
<%@ page pageEncoding="UTF-8" %>
<%@page import="java.util.Date, java.io.*"%>
<%@ taglib tagdir="/WEB-INF/tags/" prefix="tag"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="/WEB-INF/missing.tld" prefix="missing" %>
<html>
    <head><title>JSP CC Page</title></head>
    <body>
        <h1 title="Session Creation time">
        index.jsp - <%= new java.util.Date(session.getCreationTime()).toGMTString()%></h1>

        <missing:SimpleTag name="My Name"/>
        <tag:MyTag title="My title"/> 
        <%! int run(){return 0;} %>
<%
    String var = "str";
    var.getBytes(); /*CC var.| */
    int i = 0;
    char ch = 'a';
    if (true) {
        switch (aa) {
            case 0: a = b;
            default: run ();
        }
    }
    // comment
%>

<%-- comment --%>
<!-- a text -->
<%
    var.compareTo("AA"); //CC var.|
%>

        <jsp:useBean id="var2" scope="request" class="java.util.Date"/>
        <%-- jsp:useBean id="var2" scope="request" class="| --%>

        Date2: <%= System.currentTimeMillis()%><%--CC System.| --%>
        Params: <%= request.getParameterMap()%><%--CC request.| --%>

        <%-- html comments --%>
        <!--
        <jsp:include page="yyy.jsp" flush="false"/><%-- CC <jsp:include page="| --%>
<%
    var.equals("aa"); //CC var.|
%>
        -->
        <jsp:include page="yyy.jsp" flush="false"/> 
        \#{tohle neni el}
        '$'{ text }
        ${'${'}exprA}

        <ul>
            <c:forTokens items="a,b,c" delims="," var="token">
                <li>${token}
            </c:forTokens>
            <a href="link">\${ tohle neni expression language}</a>
            
            <a href="link">#{...}\{..}</a>
        </ul>
    </body>
</html>
