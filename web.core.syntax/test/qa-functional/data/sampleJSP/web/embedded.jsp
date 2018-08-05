
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style>
            #foo{
                color:red
            }
            .bar{}
            div > p > .test > .bar2{/*cc;13;;0;color,clip;;caption-side;*/
                
            }
            /*cc;16;;0;a,abbr,@charset;;;*/
            
            /*cc;18;.;0;bar,test;foo;bar;*/
            
            .test[title]{
            /*cc;21;c;0;color,clip;transform,font-size;;*/
            
            }
            p{
            /*cc;25;color: ;0;red,green,blue;font-size;red;*/
            
            }
            p{
            /*cc;29;-;0;-webkit-animation,-moz-animation,-ms-accelerator;color;;*/
            
            }
            
            
        </style>
        <title>JSP Page</title>
    </head>
    <body>
        <script>
            function Test() {
                this.name = "dummy";
                this.date = new Date();
                this.total = "";
                var test = {
                    attempt: 1,
                    run: new Date()
                };
                //cc;LINE;TEXT;BACK;RESULT;NEGATIVE_RESULT;PREFIX
                //cc;48;;0;arguments,name,date,test,Math,function,inner;;test
                
                //cc;50;this.;0;date,name,total,print;test;;
                
                this.print = function() {
                    return "what";
                };

                function inner() {
                //cc;57;t;0;test,total;name;;
                
                //cc;59;test.;0;attempt,run;;;
        
                }
                
                //cc;63;this.date.;0;UTC,getDay;;;
                
            }

            var a = new Test();
            //cc;68;a.;0;name,date,total,print;inner,test;;

            //cc;70;a.date.;0;UTC,getDay;;;
            
        </script>
        <h1>Hello World!</h1>
    </body>
</html>
