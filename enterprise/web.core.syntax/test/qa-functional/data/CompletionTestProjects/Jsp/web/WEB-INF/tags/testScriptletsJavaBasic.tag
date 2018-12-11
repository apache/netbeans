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
Integer getId ()
<% testBean.getId()
--%>

<%-- completion for String object inside scriptlets --%>
<%--CC
<% "Hello World !".| %>
int hashCode ()
<% "Hello World !".hashCode() %>
--%>

</body>
</html>
