<%@tag pageEncoding="UTF-8"%>

<%@ tag import="java.util.LinkedList"%>
<%@ tag import="org.test.Card"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Scriptlets Code Completion Page</title>
    </head>
    <body>

    <h1>TAG Scriptlets Code Completion Page</h1>

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
<% LinkedList l = new LinkedList();l.|
void clear ()
<% LinkedList l = new LinkedList();l.clear();
--%>

<%-- issue 91026: [cc] Object methods code completion doesn't work in scriptlets for imported classes --%>
<%--CC
<% org.test.TestBean t = new org.test.TestBean(); t.|
void setName (String name
<% org.test.TestBean t = new org.test.TestBean(); t.setName(name)
--%>

    </body>
</html>
