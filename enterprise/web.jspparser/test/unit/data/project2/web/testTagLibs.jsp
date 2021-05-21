<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags/"%>
<%@taglib prefix="myTags" tagdir="/WEB-INF/tags/myTags/"%>
<%@taglib prefix="jarTags" uri="/TestTagLibrary"%>
<html>
<head><title>Test for taglib and tagfiles</title></head>
<body>
<tags:linklist separator="/"/>
<myTags:testx a="hello" />

</body>
</html>
