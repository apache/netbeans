<%@page import="test.SimpleBean, test.InnerBean" contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <style>
            .rule{
                font-weight: 100;

            }
        </style>
    </head>
    <body>
        <jsp:useBean class="test.SimpleBean" id="simplebean" scope="page"/><jsp:useBean class="test.InnerBean" id="innerBean" scope="page"/>

        <h1>Hello World!</h1>
        
        
        <script>

            function Test() {
                this.name = "dummy";
                this.total = "";
                this.print = function() {
                    return "what";
                };
            }

            var a = new Test();


        </script>
    </body>
</html>
