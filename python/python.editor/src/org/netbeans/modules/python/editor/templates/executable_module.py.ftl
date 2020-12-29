<#if python3style?? && python3style>
#!/usr/bin/env python3
<#else>
#!/usr/bin/env python2
</#if>
<#if encoding??>
# -*- coding: utf-8 -*-
</#if>

<#-- This is a FreeMarker template -->
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
