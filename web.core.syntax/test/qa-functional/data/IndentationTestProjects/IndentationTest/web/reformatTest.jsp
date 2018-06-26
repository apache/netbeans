<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" 
    prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="f" %>
<html>
        <head>
<title>Localized Dates</title></head>
           <body bgcolor="white">
<jsp:useBean id="locales" scope="application"
  class="java.lang.String"/>

                <%
                int j = 3;
                switch(j){
            case 10:
break;
            case 20:
break;
default:

      }

                %>
            <form name="localeForm" action="index.jsp" method="post">
<c:set var="selectedLocaleString" value="${param.locale}" />
     <c:set var="selectedFlag" 
  value="${!empty selectedLocaleString}" />
<b>Locale:</b>
  <select name=locale>
<c:forEach var="localeString" items="${locales.localeNames}" >
    <c:choose>
  <c:when test="${selectedFlag}">
  <c:choose>
      <c:when test="${f:contains('AUTOBUS', 'BUS')}" >
        <option selected>${localeString}</option>
  </c:when>
      <c:otherwise>
      <%= 
                           "AHOJ"
             %>
  <option>${localeString}</option>
  </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <option>${localeString}</option>
  </c:otherwise>
           </c:choose>
                           </c:forEach>
</select>
<input type="submit" name="Submit" value="Get Date">
</form>

<c:if test="${selectedFlag}" >
<jsp:setProperty name="locales"
    property="selectedLocaleString"
    value="${selectedLocaleString}" />
<jsp:useBean id="date" class="java.util.Date"/>
  <jsp:setProperty name="date" property="locale"
    value="${locales.selectedLocale}"/>
  <b>Date: </b>${date.date}
</c:if>
<%! public int fce(int a, int b, int c){/*COMMENT */
    return 0;
   }
%>

<%
    if ((Integer.MAX_VALUE == Integer.MIN_VALUE) && (Boolean.TRUE == Boolean.FALSE)){
for (int i = 0; i < Integer.MAX_VALUE; ++i){
                                            if ( i > Byte.SIZE ){
                    out.println("i:" + i);
                    }
}
    }
%>
</body>
</html> 