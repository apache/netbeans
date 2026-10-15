<%@page contentType="text/html;charset=UTF-8"%>
<%@page isELEnabled="true"%>
<%@page isScriptingEnabled="false"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/META-INF/fmt.tld" %>

<h3>Enter data for the cookie</h3>
<form method="get" action="CookieMake.jsp">
<table>
<tr>
    <td><b>Name:</b></td>
    <td><input type="text" size="20" name="name" value=""></td>
</tr>
<tr>
    <td><b>Value:</b></td>
    <td><input type="text" size="20" name="value" value=""></td>
</tr>
<tr>
    <td><b>Domain:</b></td>
    <td><input type="text" size="20" name="domain" value=""></td>
</tr>
<tr>
    <td><b>Path:</b></td>
    <td><input type="text" size="20" name="path" value=""></td>
</tr>
<tr>
    <td><b>Max Age:</b></td>
    <td><input type="text" size="20" name="maxage" value="360"></td>
</tr>
<tr>
    <td><b>Secure:</b></td>
    <td><input type="checkbox" name="secure"></td>
</tr>
<tr>
    <td>&nbsp;</td>
  
</tr>
</table>
  <input type="submit" value="Make me a cookie now">
</form>

