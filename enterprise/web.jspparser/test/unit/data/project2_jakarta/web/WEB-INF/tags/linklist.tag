<%@taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>
<%@attribute name="separator" required="true" fragment="true"%>


<a href="/docs/index.jsp"><fmt:message key="home" bundle="${bundle}"/></a>
<jsp:invoke fragment="separator"/>
<a href="/docs/parameters/Input.jsp"><fmt:message key="parameters" bundle="${bundle}"/></a>
<jsp:invoke fragment="separator"/>
<a href="/docs/cookies/CookieCutter.jsp"><fmt:message key="cookies" bundle="${bundle}"/></a>
<jsp:invoke fragment="separator"/>
<a href="/docs/localized/Babel.jsp"><fmt:message key="localized" bundle="${bundle}"/></a>


<jsp:doBody/>
