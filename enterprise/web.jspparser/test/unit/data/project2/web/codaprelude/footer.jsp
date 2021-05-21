    <%
        list.add("Added in footer\n");
    %>
    Footer was executed <br/>
    <hr/>
    Content of the List:
    <table>
        <c:forEach items="<%= list %>" var="item">
            <tr><td>Item: <c:out value="${item}"/></td></tr>
        </c:forEach>
    </table>
    </body>
</html>
