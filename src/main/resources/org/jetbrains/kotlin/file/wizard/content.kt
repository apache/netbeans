<#if package?? && package != "">
package ${package}

<#assign licenseFirst = "/*">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

</#if>
/*

  @author ${user}
  Created on ${date}
*/