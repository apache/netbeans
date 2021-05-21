<%@taglib prefix="ee" uri="/WEB-INF/tlds/lib.tld" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <jsp:forward page="WEB-INF/tags/newtag_file.tag"></jsp:forward>
        <jsp:include page="index.jsp"/>
        <h1>Hello World!</h1>
        <ee:newtag_file></ee:newtag_file>
        <%
            String a = "";
            %>
    </body>
</html>
