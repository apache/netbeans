<%@page import="test.SimpleBean, test.InnerBean" contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
         <jsp:useBean class="test.SimpleBean" id="simplebean" scope="page"/><jsp:useBean class="test.InnerBean" id="innerBean" scope="page"/>
        
         
        ${"a"+"b"}
        ${simplebean["setMsg"]("test");simplebean["setMsg"]("test2")}
        ${1 mod 3}
        ${1 % 3}
        ${1 gt 3}
        ${true && true and true || false or false}
        ${true}
        ${a=1}
        ${v = (x,y)->x+y; v(1,2)}
        ${((x,y)->x+y)(3,4)} 

        ${v = {1,2}}
        ${v = {"one":1, "two":2, "three":3}}
        ${true?1:2}
        
    </body>
</html>
