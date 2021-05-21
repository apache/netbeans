<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page  import="java.util.LinkedList" %>
<%@ page import="org.test.Card" %>
<%@ page import="static java.util.Calendar.*" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

    <h1>JSP Page</h1>
    <%
        LinkedList<Boolean> list = new LinkedList<Boolean>();
        String[] a; 
        Card.Suit suit;
    %>
    
<%-- Generic types --%>
<%--CC
<% LinkedList<B|
Boolean
<% LinkedList<Boolean
--%>

<%--CC
<% list.|
Boolean element ()
<% list.element()
--%>

<%-- For Each Loop --%>
<%--CC
<% for (Boolean b : list)  b.|
boolean booleanValue ()
<% for (Boolean b : list)  b.booleanValue()
--%>

<%--CC
<% for (String str : a) a.|
int length
<% for (String str : a) a.length
--%>

<%-- ENUMs --%>
<%--CC
<% suit.|
Suit DIAMONDS
<% suit.DIAMONDS
--%>


<%-- Annotations(user defined) --%>
<%--CC
<% @Card.
RequestForEnhancement
<% @Card.RequestForEnhancement
--%>

<%--CC
<% @Card.RequestForEnhancement(|)%>
String date = "[unimplemented]"
<% @Card.RequestForEnhancement(date = )%>
--%>

<%--CC
<% D| %>
int DECEMBER
<% DECEMBER %>
--%>

<%--CC
<% get| %>
Calendar getInstance
<% getInstance() %>
--%>

<%-- issue 90718: [cc] class attribute of jsp:useBean should not offer static --%>
<%--CC
<jsp:useBean id="bean" scope="application" class="B|"
Boolean
<jsp:useBean id="bean" scope="application" class="Boolean"
--%>

<%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%>
    <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
    --%>
    
    </body>
</html>
