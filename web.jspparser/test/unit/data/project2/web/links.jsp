<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<!-- Parameters 

     type : history / complete / both
-->

<% 
    String type = request.getParameter("type"); 
    String buildNumber = request.getParameter("buildNumber");
%>

<p>
<table cellpadding="2" cellspacing="2" border="0">
    <thead> 
        <tr>
            <td colspan="2" rowspan="1">    <div style="margin-left: 5px; font-size: 12pt;">Links</div></td>
        </tr>
    </thead> 
    
    <tbody>
<%
            if(type.equals("history") || type.equals("both")) {
%>
    <tr>
      <td><a href="history.jsp">Performance history</a></td>
      <td>complete results in comparison with average of last 9 builds excluding current one</td>
    </tr>
<%
            } 

            if(type.equals("complete") || type.equals("both")) {
%>            
    <tr>
      <td><a href="complete_results.jsp<%= (buildNumber==null)?"":("?buildNumber="+buildNumber)%>">Complete test results</a></td>
      <td>complete results of all measured values on last build</td>
    </tr>
<%
            }
%>            
  </tbody>
</table>
</p>

