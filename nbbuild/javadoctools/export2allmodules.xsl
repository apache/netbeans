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

The Original Software is NetBeans. The Initial Developer of the Original
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

    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>All NetBeans Classes</title>
           <link rel="stylesheet" href="org-openide-util/javadoc.css" type="text/css"/>
        </head>

        <body>


        <TABLE BORDER="0" WIDTH="100%" CELLPADDING="1" CELLSPACING="0" SUMMARY="">
        <TR>
        <TD COLSPAN="2" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
        <A NAME="navbar_top_firstrow"><!-- --></A>
        <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="3" SUMMARY="">
          <TR ALIGN="center" VALIGN="top">
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">overview-summary.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>Overview</B></FONT>
            </a>
          </TD>
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">allclasses-frame.html</xsl:attribute>
                <xsl:attribute name="target">packageFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>AllClasses</B></FONT>
            </a>
          </TD>
          <TD BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    
            <a>
                <xsl:attribute name="href">usecases.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                <FONT CLASS="NavBarFont1"><B>UseCases</B></FONT>
            </a>
          </TD>
          </TR>
        </TABLE>
        </TD>
        <TD ALIGN="right" VALIGN="top" ROWSPAN="3"><EM>
        </EM>
        </TD>
        </TR>
        </TABLE>
        
        <TABLE BORDER="0" WIDTH="100%" SUMMARY="">
        <TR>
        <TD NOWRAP=""><FONT CLASS="FrameItemFont">
            <xsl:for-each select="//module[not (@name = '_no module_')]" >
                <xsl:sort order="ascending" select="@name" />
                <xsl:call-template name="module" />
            </xsl:for-each>
        </FONT></TD>
        </TR>
        </TABLE>
        
        </body>
        </html>
    </xsl:template>
    
    <xsl:template name="module">
        <span>
            <xsl:attribute name="style">
                <xsl:choose>
                    <xsl:when test="descendant::api[@category='stable' and @group='java']">background:#ffffff</xsl:when>
                    <xsl:when test="descendant::api[@category='official' and @group='java']">background:#ffffff</xsl:when>
                    <xsl:when test="descendant::api[@category='devel' and @group='java']">background:#ddcc80</xsl:when>
                    <xsl:when test="descendant::api[@category='deprecated' and @group='java']">text-decoration: line-through</xsl:when>
                    <xsl:otherwise>background:#e0c0c0</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <a>
                <xsl:attribute name="href"><xsl:value-of select="substring-before(@target,'/')" />/allclasses-frame.html</xsl:attribute>
                <xsl:attribute name="target">packageFrame</xsl:attribute>

                <xsl:value-of select="@name" />
            </a>
            (<a>
               <xsl:attribute name="href"><xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>
                javadoc
            </a>)
        </span>
        <br/>
    </xsl:template>
    
</xsl:stylesheet>


