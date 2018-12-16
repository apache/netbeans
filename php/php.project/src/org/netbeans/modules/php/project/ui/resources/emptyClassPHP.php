<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

<#if namespace?? && namespace?length &gt; 0>
namespace ${namespace};
</#if>

/**
 * Description of ${name}
 *
 * @author ${user}
 */
class ${name} {
    //put your code here
}
