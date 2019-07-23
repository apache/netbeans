<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script type="text/javascript">
    function f()
    {
        <c:url value="" value="${param.dd}">
            <c:param name="bar" value="${param.value}"/>
        </c:url>
    }
</script>
