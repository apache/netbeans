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
    Document   : tld-frame.html.xsl
    Created on : December 18, 2002, 11:40 AM
    Author     : mroth
    Description:
        Creates the TLD frame (lower-left hand corner), listing the tags
        and functions that are in this particular tag library.
-->

<xsl:stylesheet version="1.0"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    
    <xsl:output method="html" indent="yes"/>
    
    <xsl:param name="tlddoc-shortName">default</xsl:param>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <xsl:apply-templates select="javaee:tlds/javaee:taglib"/>
    </xsl:template>
    
    <xsl:template match="javaee:taglib">
      <xsl:if test="javaee:short-name=$tlddoc-shortName">
        <xsl:variable name="tldname">
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
        </xsl:variable>
        <xsl:variable name="tldfull">
          <xsl:value-of select="$tldname"/>
          <xsl:choose>
            <xsl:when test="javaee:description!=''">
              (<xsl:value-of select="javaee:description" disable-output-escaping="yes"/>)
            </xsl:when>
            <xsl:otherwise>
              No Description
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <html>
          <head>
            <title>
              <xsl:value-of select="$tldfull"/>
            </title>
            <meta name="keywords" content="$tldfull"/>
            <link rel="stylesheet" type="text/css" href="../stylesheet.css" 
                  title="Style"/>
            <script>
              function asd()
              {
              parent.document.title="<xsl:value-of select="normalize-space($tldfull)"/>";
              }
            </script>
          </head>
          <body bgcolor="white" onload="asd();">
            <font size="+1" class="FrameTitleFont">
              <a href="tld-summary.html" target="tagFrame">
                <xsl:value-of select="$tldname"/>
              </a>
            </font>
            <table border="0" width="100%">
              <xsl:if test="(count(javaee:tag)+count(javaee:tag-file))>0">
                <tr>
                  <td nowrap="true">
                    <font size="+1" class="FrameHeadingFont">
                      Tags
                    </font>&#160;
                    <font class="FrameItemFont">
                      <xsl:apply-templates select="javaee:tag|javaee:tag-file"/>
                    </font>
                  </td>
                </tr>
              </xsl:if>
              <xsl:if test="count(javaee:function)>0">
                <tr>
                  <td nowrap="true">
                    <font size="+1" class="FrameHeadingFont">
                      Functions
                    </font>&#160;
                    <font class="FrameItemFont">
                      <xsl:apply-templates select="javaee:function"/>
                    </font>
                  </td>
                </tr>
              </xsl:if>
              <xsl:if test="count(javaee:validator)>0">
                <tr>
                  <td nowrap="true">
                    <font size="+1" class="FrameHeadingFont">
                      Validator
                    </font>&#160;
                    <font class="FrameItemFont">
                      <xsl:apply-templates select="javaee:validator"/>
                    </font>
                  </td>
                </tr>
              </xsl:if>
              <xsl:if test="count(javaee:listener)>0">
                <tr>
                  <td nowrap="true">
                    <font size="+1" class="FrameHeadingFont">
                      Listeners
                    </font>&#160;
                    <font class="FrameItemFont">
                      <xsl:apply-templates select="javaee:listener"/>
                    </font>
                  </td>
                </tr>
              </xsl:if>
            </table>
            <!-- <table ... -->
          </body>
        </html>
      </xsl:if>
    </xsl:template>
    
    <xsl:template match="javaee:tag|javaee:tag-file">
      <br/>
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="javaee:name"/>.html</xsl:attribute>
        <xsl:attribute name="target">tagFrame</xsl:attribute>
        <xsl:value-of select="../javaee:short-name"/>:<xsl:value-of select="javaee:name"/>
      </xsl:element>
    </xsl:template>
    
    <xsl:template match="javaee:function">
      <br/>
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="javaee:name"/>.fn.html</xsl:attribute>
        <xsl:attribute name="target">tagFrame</xsl:attribute>
        <i><xsl:value-of select="../javaee:short-name"/>:<xsl:value-of select="javaee:name"/>()</i>
      </xsl:element>
    </xsl:template>
    
    <xsl:template match="javaee:validator">
      <br/>
      <xsl:value-of select="javaee:validator-class"/>
    </xsl:template>
    
    <xsl:template match="javaee:listener">
      <br/>
      <xsl:value-of select="javaee:listener-class"/>
    </xsl:template>
    
</xsl:stylesheet> 
