<%@page contentType="text/html"%>
<html>
<head><title>JSP Page</title></head>
<body>

<jsp:useBean id="bean1" scope="session" class="more_for_test.TestBean" />
<jsp:getProperty name="bean1"  property="sampleProperty" />

</body>
</html>
