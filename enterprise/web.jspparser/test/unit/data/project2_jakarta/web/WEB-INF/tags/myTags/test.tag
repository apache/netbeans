<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
  <jsp:directive.tag description="This is description for the test.tag file."/>  
  <jsp:directive.attribute name="a"
                           type="java.lang.String"
                           required="true"/>

  <jsp:text>A sample tag: ${a}</jsp:text>
</jsp:root>
