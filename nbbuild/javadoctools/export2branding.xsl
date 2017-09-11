<?xml version="1.0" encoding="UTF-8" ?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the 77Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:param name="date" />

    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>Description Layer Registration in NetBeans APIs</title>
            <link rel="stylesheet" href="netbeans.css" type="text/css"/>

          <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
          <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

        </head>

        <body>
            <center><h1>Description of Branding APIs for NetBeans Platform Applications</h1></center>

            <p>
            Applications built on top of NetBeans Platform may change configuration
            of various aspects via branding. E.g. by providing different values
            for certain keys in various <code>Bundle</code>. 
            This page summarizes the list of such
            branding APIs defined by <a href="index.html">modules with API</a>. 
            </p>

            <ul>
            <xsl:for-each select="//api[@type='export' and @group='branding']" >
                <li>
                    <b>
                        <xsl:choose >
                            <xsl:when test="@url" >
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="@url"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="@name"></xsl:value-of>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@name"></xsl:value-of>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> in </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="ancestor::module/@target"/>
                                <xsl:text>#group-branding</xsl:text>
                            </xsl:attribute>
                            <xsl:value-of select="ancestor::module/@name"/>
                        </a>
                    </b>
                    <p>
                    <xsl:apply-templates select="." />
                    </p>
                </li>
            </xsl:for-each>
            </ul>
            
            <p>
                To get your API listed here, use 
                <code>&lt;api type='export' group='branding' ... /&gt;</code> in
                your module arch.xml document.
            </p>
         </body>
         </html>
    </xsl:template>
    
    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <xsl:template match="usecase">
        <h4><xsl:value-of select="@name" /></h4>
        <xsl:apply-templates select="./node()" />
    </xsl:template>

    <xsl:template match="a[@href]">
        <xsl:variable name="target" select="ancestor::module/@target"/>
        <xsl:variable name="top" select="substring-before($target,'/')" />
        
          <xsl:call-template name="print-url" >
            <xsl:with-param name="url" select="@href" />
            <xsl:with-param name="base" select="$target" />
            <xsl:with-param name="top" select="$top" />
          </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="print-url" >
        <xsl:param name="url" />
        <xsl:param name="base" />
        <xsl:param name="top" />
        
        <xsl:choose>
            <xsl:when  test="contains(@href,'@TOP@')" >
                <xsl:comment>URL contains @TOP@</xsl:comment>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$top" />
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after($url,'@TOP@')" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:when test="contains($url,'//')" >
                <xsl:comment>This is very likely URL with protocol, if not see nbbuild/javadoctools/export2usecases.xsl</xsl:comment>
                <a> 
                    <xsl:attribute name="href">
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:when test="starts-with($url, '#')" >
                <xsl:comment>Probably reference in the same target document</xsl:comment>
                <a href="{$base}{$url}" >
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:when test="starts-with($url, 'org-')" >
                <xsl:comment>Probably reference relative to root</xsl:comment>
                <a href="{$url}" >
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment>This must be a reference relative to the arch page, if not see nbbuild/javadoctools/export2usecases.xsl
            </xsl:comment>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$base" />
                        <xsl:text>/../</xsl:text>
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
            
    <xsl:template match="@*|node()">
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>
        
</xsl:stylesheet>


