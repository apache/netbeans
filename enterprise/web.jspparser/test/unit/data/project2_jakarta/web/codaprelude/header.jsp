<%-- 
    Document   : header
    Created on : Aug 21, 2007, 11:32:03 AM
    Author     : petr
--%>
<%@page import="java.util.ArrayList" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <% 
        ArrayList<String> list = new ArrayList<String>();
        list.add("Added in header\n");
    %>
    <body>
    Header was executed <br/>
    