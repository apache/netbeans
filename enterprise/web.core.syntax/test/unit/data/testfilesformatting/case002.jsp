<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html style="color: aqua;
background:silver;">
<head>
<% String s = "red"; %>
<style>
a {
color:
blue,
<%= toString() %>,
red;
}
h1 {
background: <%=s %>;

}
</style>
<style>
h2 {
<%=s %>: red <%=s %>;
}
</style>
</head>
<body>
<h1><%=System.currentTimeMillis() %></h1>
</body>
</html>
