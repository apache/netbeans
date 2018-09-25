<%@page contentType="text/html"%>
<%@page import="java.util.Properties, java.util.Enumeration" pageEncoding="UTF-8"%>

<%--

siteInfo.jsp

Created on 09 November 2006

$Id$

--%>

<%
String error = null;
Properties systemProperties = null;
Enumeration systemPropertiesEnum = null;
Enumeration servletInitParametersEnum = null;

ServletContext sc = getServletContext ();

ServletConfig scg = getServletConfig ();

servletInitParametersEnum = scg.getInitParameterNames ();

String serverInfo = sc.getServerInfo ();
int majorApiVersion = sc.getMajorVersion ();
int minorApiVersion = sc.getMinorVersion ();

try {
systemProperties = System.getProperties ();
systemPropertiesEnum = (Enumeration) systemProperties.propertyNames ();
} catch (Exception e){
error = e.getMessage ();
}
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Java Site Information</title>
</head>
<body>

<h1>Java Site Information</h1>

<div>

<%
out.println ("The system container is " + serverInfo + "<br />");
out.println ("<hr />");

if (servletInitParametersEnum != null){
out.println ("<p>The Servlet initialization parameters are: </p>");

out.println ("<table>");
while(servletInitParametersEnum.hasMoreElements ()){
String prop = servletInitParametersEnum.nextElement ().toString ();
out.print ("<tr><td><b>" + prop + "</b>" + "</td>");
out.println ("<td>" + scg.getInitParameter (prop) + "</td></tr>");
}
out.println ("</table>");
}else {
out.println("No Servlet Init Parameters Available.<br />");
}

out.println ("<hr />");
out.println ("<table>");

if (error == null){
while (systemPropertiesEnum.hasMoreElements ()){
String prop = systemPropertiesEnum.nextElement ().toString ();
out.print ("<tr><td><b>" + prop + "</b>" + "</td>");
out.println ("<td>" + systemProperties.getProperty (prop) + "</td></tr>");
} // end while
} // end if
else {
out.println ("An exception was thrown: <b>" + error + "</b>");
}
%>
</div>


</body>
</html>
