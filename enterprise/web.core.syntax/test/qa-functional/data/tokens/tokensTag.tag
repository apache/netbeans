<%@tag description = "put the tag description here" pageEncoding="UTF-8"%>

<%-- Taglib directives can be specified here: --%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags/" prefix="tags"%>

<%-- The list of normal or fragment attributes --%>

<%@attribute name="name" required="true"%>
<%@ attribute name="items" required="true"%>
<%@attribute name="size" required="true" %>
<%@ attribute name="multiple" %>

<%!  javax.xml.transform.TransformerFactory con; //CC javax.|%>
<%!
public String time (int i) {
    if (i==0) {
        return "relax".toUpperCase(); //CC "relax".|
    } else {
        return "time:" + System.currentTimeMillis(); //CC System.|
    }
}%>

<!-- a text -->
<jsp:element name="select">
    <jsp:attribute name="name">${name}</jsp:attribute>
    <jsp:attribute name="size">${size}</jsp:attribute>
    <jsp:attribute name='multiple' />
    <jsp:body>
        <c:forTokens items="${items}" delims=":," var="item">
            <option>${item}
        </c:forTokens>
    </jsp:body>
</jsp:element>

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




