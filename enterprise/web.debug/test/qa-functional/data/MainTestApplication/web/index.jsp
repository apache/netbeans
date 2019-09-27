<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://test.netbeans.org/tags/testlibrary" prefix="ttl"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="custom"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%-- One line JSP comment --%>
<%-- Multi line 
    JSP comment --%>
<!-- One line HTML comment -->
<!-- Multi line 
    HTML comment -->
    <% int i = 0; %>
    
    <% 
       String name =
               "very long name";
    %>
<html> 
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test JSP Page</title>
    </head>
    <body>

    <h1>MainTestApplication index.jsp</h1>
    
<hr/>
<font style="color: red"><b>Using tag libraries</b><br></font>
<hr/>
<ttl:hello name="User">
Hello user!
</ttl:hello>

<hr/>

<ttl:sum x="1" y="2">
Content of sum(x,y) body = sum(1,2).
</ttl:sum>
<hr/>

<custom:mytable title="NetBeans Tutorial" author="NetBeans Community">
Description of NetBeans Tutorial.
</custom:mytable>

<hr/>
<font style="color: red"><b>Using java libraries</b><br></font>
<hr/>
<%    org.netbeans.test.testlibrary.support.SumNumbers sn = new org.netbeans.test.testlibrary.support.SumNumbers(7, 8); %>

Library sum 7 + 8 = <%= sn.getSum() %>.

<hr/>
<h2>Use freeform library:</h2>
<%
    org.netbeans.test.freeformlib.SumNumbers snFreeform = 
                        new org.netbeans.test.freeformlib.SumNumbers(10, 13);
%>

Library sum 10 + 13 = <%= snFreeform.getSum() %>.

<hr/>
<font style="color: red"><b>Using dynamic include:</b><br></font>
<%
    String jsp_include = "simpleDynamicInclude";
%>
<jsp:include page="<%= "incl/" + jsp_include + ".jsp"%>"/>
<jsp:include page="incl/embeddedDynamicInclude.jsp"/>


<hr/>
<font style="color: red"><b>Using include directive:</b><br></font>
<%@include file="incl/simpleInclude.jsp"%>
<%@include file="incl/simpleInclude.jsp"%>
<%@include file="incl/subincl/simpleInclude.jsp"%>
<hr/>

<font style="color: red"><b>Embedded include:</b><br></font>
<%@include file="incl/embeddedInclude.jsp"%>

<hr/>

<form method="get" action="DivideServlet">
    <input type="text" name="x" value="1"/>
    <input type="text" name="y" value="2"/>
    <input type="submit" value="Divide"/>
</form>

<hr/>

<form method="get" action="Multiply">
    <input type="text" name="x" value="2"/>
    <input type="text" name="y" value="3"/>
    <input type="submit" value="Multiply"/>
</form>

<hr/>
<%@include file="WEB-INF/jspf/logo.jspf"%>

<hr/>
<h2>Attributes:</h2>
<%
    session.setAttribute("MySessionAttribute", "MySessionAttributeValue");
    request.setAttribute("MyRequestAttribute", "MyRequestAttributeValue");
%>

Session attribute = <%= session.getAttribute("MySessionAttribute") %>
<br>
Request attribute = <%= request.getAttribute("MyRequestAttribute") %>
<hr/>
<font style="color: red"><b>Using expression language:</b><br></font>
\${1 + 2}=${1 + 2}
<br>
\${-4 - 2}=${-4 - 2}
${param.sayHello}
<hr/>
<font style="color: red"><b>Using bean:</b><br></font>
<jsp:useBean id="mybean" scope="session" class="org.netbeans.test.MyBean" />
Current time: 
<%=  mybean.getTime() %>
    </body>
    
</html>