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

        <ul>
            <c:forTokens items="a,b,c" delims="," var="token">
                <li>${token}
            </c:forTokens>
        </ul>
        
            <h1>JSP 2.0 Expression Language - Basic Comparisons</h1>
    <hr>
    This example illustrates basic Expression Language comparisons.
    The following comparison operators are supported:
    <ul>
      <li>Less-than (&lt; or lt)</li>
      <li>Greater-than (&gt; or gt)</li>
      <li>Less-than-or-equal (&lt;= or le)</li>
      <li>Greater-than-or-equal (&gt;= or ge)</li>
      <li>Equal (== or eq)</li>
      <li>Not Equal (!= or ne)</li>
    </ul>
    <blockquote>
      <u><b>Numeric</b></u>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>
	  <tr>
	    <td>\${1 &lt; 2}</td>
	    <td>${1 < 2}</td>
	  </tr>
	  <tr>
	    <td>\${1 lt 2}</td>
	    <td>${1 lt 2}</td>
	  </tr>
	  <tr>
	    <td>\${1 &gt; (4/2)}</td>
	    <td>${1 > (4/2)}</td>
	  </tr>
	  <tr>
	    <td>\${1 &gt; (4/2)}</td>
	    <td>${1 > (4/2)}</td>
	  </tr>
	  <tr>
	    <td>\${4.0 &gt;= 3}</td>
	    <td>${4.0 >= 3}</td>
	  </tr>
	  <tr>
	    <td>\${4.0 ge 3}</td>
	    <td>${4.0 ge 3}</td>
	  </tr>
	  <tr>
	    <td>\${4 &lt;= 3}</td>
	    <td>${4 <= 3}</td>
	  </tr>
	  <tr>
	    <td>\${4 le 3}</td>
	    <td>${4 le 3}</td>
	  </tr>
	  <tr>
	    <td>\${100.0 == 100}</td>
	    <td>${100.0 == 100}</td>
	  </tr>
	  <tr>
	    <td>\${100.0 eq 100}</td>
	    <td>${100.0 eq 100}</td>
	  </tr>
	  <tr>
	    <td>\${(10*10) != 100}</td>
	    <td>${(10*10) != 100}</td>
	  </tr>
	  <tr>
	    <td>\${(10*10) ne 100}</td>
	    <td>${(10*10) ne 100}</td>
	  </tr>
	</table>
      </code>
      <br>
      <u><b>Alphabetic</b></u>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>
	  <tr>
	    <td>\${'a' &lt; 'b'}</td>
	    <td>${'a' < 'b'}</td>
	  </tr>
	  <tr>
	    <td>\${'hip' &gt; 'hit'}</td>
	    <td>${'hip' > 'hit'}</td>
	  </tr>
	  <tr>
	    <td>\${'4' &gt; 3}</td>
	    <td>${'4' > 3}</td>
	  </tr>
	</table>
      </code>
    </blockquote>
        
    </body>
</html>
