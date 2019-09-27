<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>All Apache NetBeans (incubating) Classes</title>
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


