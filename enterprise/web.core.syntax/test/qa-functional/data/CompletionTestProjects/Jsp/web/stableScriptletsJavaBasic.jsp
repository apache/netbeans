<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ page import="java.util.LinkedList"%>
<%@ page import="org.test.Card"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Scriptlets Code Completion Page</title>
    </head>
    <body>

    <h1>JSP Scriptlets Code Completion Page</h1>

<%-- Java completion for TestBean class --%>
<%--CC
<% org.test.|
TestBean
<% org.test.TestBean
--%>

<% org.test.TestBean testBean; %>

<%-- Java completion for variables between more sriptlets --%>
<%--CC
<% t|
TestBean testBean
<% testBean
--%>

<%-- Java completion for variables methods and fields between more sriptlets --%>
<%--CC
<% testBean.| %>
void setName
<% testBean.setName(name); %>
--%>


    </body>
</html>
