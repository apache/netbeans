<%@tag description = "This is description for the first tag" pageEncoding="UTF-8" %>

<%-- Taglib directives can be specified here: --%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
--%>

<%-- The list of normal or fragment attributes can be specified here: --%>

<%@attribute name="title" required="true"%>
<%@attribute name="author"%>
<%@attribute name="price_info" fragment="true"%>


<%-- Use expression language to work with normal attributes or use --%>
<%-- the <jsp:invoke> and <jsp:doBody> actions for invoking JSP fragments and tag body: --%>
<%--
<table border=1>
  <tr>
    <td align="center"><h2>${title}</h2></td>
  </tr>
  <tr>
    <td><i>Author:</i> <b>${author}</b></td>
  </tr>
  <tr>
    <td><i>Price Info:</i> <jsp:invoke fragment="price_info"/></td>
  </tr>
  <tr>
    <td collspan=2><i>Book Info:</i> <jsp:doBody/></td>
  </tr>
</table>
--%>
