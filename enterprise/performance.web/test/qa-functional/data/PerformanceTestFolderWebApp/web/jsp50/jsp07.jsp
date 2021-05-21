<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.Date"%>

<html>
<head>
  <title>JSP Page</title>
</head>

<body>

<h1>Testing JSP</h1>

<i>This is JSP which contains as many as possible JSP structures.</i><br>

<i>Now we are testing HTML comment which should appear in page.</i><br>
<!-- one line HTML comment -->

<i>And now HTML comment split to more lines.</i><br>
<!-- this is HTML comment
     split to 
     more lines -->
     
<i>Now we are testing JSP comment which should not appear in page.</i><br>    
<%-- one line JSP comment --%>

<i>And now JSP comment splt to more lines.</i><br>
<%-- this is JSP comment
     split to 
     more lines --%>

<i>Test of variable declaration.</i><br>     
<%! int i = 1; %>

<i>Test of multiple variable declaration.</i><br>     
<%! int a = 0;
    int b;
    int c = 1;
%>

<i>Test of one line scriptlet.</i><br>     
<%  out.println("This is output from scriptlet<br>"); %>

<i>Test of mutliple line scriptlet.</i><br>
<%  out.println("This is output from another scriptlet<br>"); 
    Date date = new Date();
    out.println("Today is "+date.toString()+"<br>");
    a = i + i;
    b = 2; c = b * b;
    out.println("1 + 1 = "+a+"<br>");
    out.println("2 * 2 = "+c+"<br>");
%>

<i>Test of expression.</i><br>
Free memory: <%=Runtime.getRuntime().freeMemory()%><br>

Used memory: <%=Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory()%><br>
     
<i>Test of beans.</i><br>     
<jsp:useBean id="bean" scope="session" class="test.TestBean" />
<jsp:setProperty name="bean" property="intProperty" value="100"/>
intProperty = <jsp:getProperty name="bean"  property="intProperty"/><br>

1 + 2 = ${1 + 2} <br>
intProperty = ${bean["intProperty"]}<br>

<h2>And once again</h2>

<i>This is JSP which contains as many as possible JSP structures.</i><br>

<i>Now we are testing HTML comment which should appear in page.</i><br>
<!-- one line HTML comment -->

<i>And now HTML comment split to more lines.</i><br>
<!-- this is HTML comment
     split to 
     more lines -->
     
<i>Now we are testing JSP comment which should not appear in page.</i><br>    
<%-- one line JSP comment --%>

<i>And now JSP comment splt to more lines.</i><br>
<%-- this is JSP comment
     split to 
     more lines --%>

<i>Test of one line scriptlet.</i><br>     
<%  out.println("This is output from scriptlet<br>"); %>

<i>Test of mutliple line scriptlet.</i><br>
<%  out.println("This is output from another scriptlet<br>"); 
    date = new Date();
    out.println("Today is "+date.toString()+"<br>");
    i = 1;
    a = i + i;
    b = 2; c = b * b;
    out.println("1 + 1 = "+a+"<br>");
    out.println("2 * 2 = "+c+"<br>");
%>

<i>Test of expression.</i><br>
Free memory: <%=Runtime.getRuntime().freeMemory()%><br>

Used memory: <%=Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory()%><br>
     
<i>Test of beans.</i><br>     
<jsp:setProperty name="bean" property="intProperty" value="200"/>
intProperty = <jsp:getProperty name="bean"  property="intProperty"/><br>

1 + 2 = ${1 + 2} <br>
intProperty = ${bean["intProperty"]}<br>

<h2>And for the last time</h2>

<i>This is JSP which contains as many as possible JSP structures.</i><br>

<i>Now we are testing HTML comment which should appear in page.</i><br>
<!-- one line HTML comment -->

<i>And now HTML comment split to more lines.</i><br>
<!-- this is HTML comment
     split to 
     more lines -->
     
<i>Now we are testing JSP comment which should not appear in page.</i><br>    
<%-- one line JSP comment --%>

<i>And now JSP comment splt to more lines.</i><br>
<%-- this is JSP comment
     split to 
     more lines --%>

<i>Test of one line scriptlet.</i><br>     
<%  out.println("This is output from scriptlet<br>"); %>

<i>Test of mutliple line scriptlet.</i><br>
<%  out.println("This is output from another scriptlet<br>"); 
    date = new Date();
    out.println("Today is "+date.toString()+"<br>");
    i = 1;
    a = i + i;
    b = 2; c = b * b;
    out.println("1 + 1 = "+a+"<br>");
    out.println("2 * 2 = "+c+"<br>");
%>

<i>Test of expression.</i><br>
Free memory: <%=Runtime.getRuntime().freeMemory()%><br>

Used memory: <%=Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory()%><br>
     
<i>Test of beans.</i><br>     
<jsp:setProperty name="bean" property="intProperty" value="200"/>
intProperty = <jsp:getProperty name="bean"  property="intProperty"/><br>

1 + 2 = ${1 + 2} <br>
intProperty = ${bean["intProperty"]}<br>

</body>
</html>
