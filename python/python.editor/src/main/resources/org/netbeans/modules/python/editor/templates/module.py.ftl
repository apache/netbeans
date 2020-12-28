<#-- This is a FreeMarker template -->
<#if encoding??>
# -*- coding: utf-8 -*-
</#if>

<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

if __name__ == "__main__":
<#if python3style?? && python3style>
    print("Hello World")
<#else>
    print "Hello World"
</#if>
