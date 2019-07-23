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
<% testBean.|
void setName
<% testBean.setName(name);
--%>

<%-- completion for String object inside scriptlets --%>
<%--CC
<% "Hello World !".| %>
int indexOf (String str )
<% "Hello World !".indexOf(str) %>
--%>

<%-- issue 90741: [cc] imported classes should be visible in Ctrl+Space cc --%>
<%--CC
<% Li|
LinkedList <E>
<% LinkedList
--%>

<%-- issue 90741: [cc] imported classes should be visible in Ctrl+Space cc --%>
<%--CC
<% C|
Card
<% Card
--%>

<%-- issue 91026: [cc] Object methods code completion doesn't work in scriptlets for imported classes --%>
<%--CC
<% LinkedList l = new LinkedList();l.| %>
void clear ()
<% LinkedList l = new LinkedList();l.clear(); %>
--%>

<%-- issue 91026: [cc] Object methods code completion doesn't work in scriptlets for imported classes --%>
<%--CC
<% org.test.TestBean t = new org.test.TestBean(); t.| %>
void setName
<% org.test.TestBean t = new org.test.TestBean(); t.setName(name); %>
--%>


    </body>
</html>
