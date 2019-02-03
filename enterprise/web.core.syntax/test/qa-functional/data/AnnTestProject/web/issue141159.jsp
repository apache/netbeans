<%-- 
    Document   : issue141159
    Created on : Sep 29, 2008, 12:13:10 PM
    Author     : jindra
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>JSP Page</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <h1 style="color: <%= "red"%>; background: green; "/>
        <h1 style="color: ${"red"}; background: green; "/>
    </body>
</html>
