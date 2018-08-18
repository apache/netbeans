<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%-- Taglib directives can be specified here: --%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>
<%@attribute name="author"%>
<%@attribute name="price_info" fragment="true"%>

<%-- Use expression language to work with normal attributes or use --%>
<%-- the <jsp:invoke> or <jsp:doBody> actions to invoke JSP fragments or tag body: --%>
<table border="1">
    <tr>
        <td align="center"><h2>${title}</h2></td>
    </tr>
    <tr>
        <td><em>Author:</em><strong>${author}</strong></td>
    </tr>
    <tr>
        <td><em>Price Info:</em><jsp:invoke fragment="price_info"/></td>
    </tr>
    <tr>
        <td colspan="2"><em>Book Info:</em><jsp:doBody/></td>
    </tr>
</table>
