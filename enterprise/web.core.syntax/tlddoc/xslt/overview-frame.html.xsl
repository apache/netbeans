<?xml version="1.0" encoding="UTF-8" ?>

<!--
  - <license>
  - Copyright (c) 2010, Oracle.
  - All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are met:
  -
  -     * Redistributions of source code must retain the above copyright
  -       notice, this list of conditions and the following disclaimer.
  -     * Redistributions in binary form must reproduce the above copyright
  -       notice, this list of conditions and the following disclaimer in the
  -       documentation and/or other materials provided with the distribution.
  -     * Neither the name of Oracle nor the names of its
  -       contributors may be used to endorse or promote products derived from
  -       this software without specific prior written permission.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  - TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
  - PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  - CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  - EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  - ROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  - PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  - LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  - NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  - SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  - </license>
  -->

<!--
    Document   : overview-frame.html.xsl
    Created on : October 1, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates the overview frame (upper left corner), listing all tag 
        libraries included in this generation.
-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee">
    
    <xsl:output method="html" indent="yes"/>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <html>
        <head>
          <title>
            Overview (<xsl:value-of select="/javaee:tlds/javaee:config/javaee:window-title"/>)
          </title>
          <link rel="stylesheet" type="text/css" href="stylesheet.css" title="Style"/>
        </head>
        <script>
          function asd() {
            parent.document.title="Overview (<xsl:value-of select="normalize-space(/javaee:tlds/javaee:config/javaee:window-title)"/>)";
          }
        </script>
        <body bgcolor="white" onload="asd();">
          <table border="0" width="100%">
            <tr>
              <td nowrap="true">
                <font size="+1" class="FrameTitleFont">
                  <b><xsl:value-of select="/javaee:tlds/javaee:config/javaee:doc-title"/></b>
                </font>
              </td>
            </tr>
          </table>
          <table border="0" width="100%">
            <tr>
              <td nowrap="true">
                <font class="FrameItemFont">
                  <a href="alltags-frame.html" target="tldFrame"><xsl:text>All Tags / Functions</xsl:text></a>
                </font>
                <p/>
                <font size="+1" class="FrameHeadingFont">
                  Tag Libraries
                </font>
                <br/>
                <xsl:apply-templates select="javaee:tlds/javaee:taglib"/>
              </td>
            </tr>
          </table>
          <p/>
        </body>
      </html>
    </xsl:template>
    
    <xsl:template match="javaee:taglib">
      <font class="FrameItemFont">
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="javaee:short-name"/>/tld-frame.html</xsl:attribute>
          <xsl:attribute name="target">tldFrame</xsl:attribute>
          <xsl:choose>
            <xsl:when test="javaee:display-name!=''">
              <xsl:value-of select="javaee:display-name"/>
            </xsl:when>
            <xsl:when test="javaee:short-name!=''">
              <xsl:value-of select="javaee:short-name"/>
            </xsl:when>
            <xsl:otherwise>
              Unnamed TLD
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </font>
      <br/>
    </xsl:template>
</xsl:stylesheet> 
