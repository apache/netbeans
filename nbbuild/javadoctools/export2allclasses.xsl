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
           <link rel="stylesheet" href="org-openide-util/javadoc.css" type="text/css" title="style" />
        </head>

        <body>
        <font size="+1" CLASS="FrameHeadingFont">
            <b>Apache NetBeans (incubating) API Classes</b>
        </font>
        
        <TABLE BORDER="0" WIDTH="100%" SUMMARY="">
        <TR>
        <TD NOWRAP=""><FONT CLASS="FrameItemFont">
        
            <xsl:for-each select="//class" >
                <xsl:sort order="ascending" select="@name" />
                <xsl:call-template name="class" />
            </xsl:for-each>
            
        </FONT></TD>
        </TR>
        </TABLE>
            
        </body>
        </html>
    </xsl:template>
    
    <xsl:template name="class">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="@url" /></xsl:attribute>
            <xsl:attribute name="target">classFrame</xsl:attribute>
            
            <xsl:choose>
                <xsl:when test="@interface = 'true'" >
                    <i><xsl:value-of select="@name" /></i>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@name" />
                </xsl:otherwise>
            </xsl:choose>
        </a>
        <br/>
    </xsl:template>
    
</xsl:stylesheet>


