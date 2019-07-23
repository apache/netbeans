<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <style type="text/css">
            h1{/*CC */
                font-size:/*CC */block;
            }
            h2{
            }/*CC */
        </style>
    </head>
    <body>
        
        <%! public int fce(int a, int b, int c){/*CC */
            return 0;
           }
        %>
        
        <%
            if ((Integer.MAX_VALUE == Integer.MIN_VALUE) && /*CC */(Boolean.TRUE == Boolean.FALSE)){
                
            }
        %>
        
        <h1><!--CC -->JSP Page</h1>
        <jsp:useBean id="myBean" scope="page" class="java.util.Date" />
        <form name="FORM_1"><!--CC -->
            <input type="text" name="imput_1" <!--CC -->value="HELLO WORLD" readonly disabled />
            <input type="radio" name="" value="NO" />
            <input type="submit" value="OK" /><!--CC -->
            <input type="reset" value="RESET" />
            <b></b>
        </form>
        <%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%><!--CC -->
        <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
    --%>
        <%@ include file="WEB-INF/web.xml"%>
        
        <%! /*CC */
        public int first(int i, String str){
            if (true){/*CC */
                    switch(i){
                        case 10:/*CC */
                            Integer.toString(10);
                        case 11:
                            i=str.length();/*CC */
                        default:
                            fce(0, 41, 8);
                    }
            }
            return 0;
        }
        %>    
        
        
    </body>
</html>
