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
    Document   : overview-summary.html.xsl
    Created on : October 1, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates an overview summary (right frame), listing all tag 
        libraries included in this generation.
-->

<xsl:stylesheet version="1.0"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    
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
          <!-- =========== START OF NAVBAR =========== -->
          <a name="navbar_top"><!-- --></a>
          <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_top_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev"> <font CLASS="NavBarFont1Rev"><b>&#160;Overview&#160;</b></font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Library&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a HREF="help-doc.html"><font CLASS="NavBarFont1"><b>Help</b></font></a>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                <!--&#160;PREV&#160;-->
                <!--&#160;NEXT&#160;-->
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="overview-summary.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
          </table>
          <!-- =========== END OF NAVBAR =========== -->
          <hr/>
          <center>
            <h2><xsl:value-of select="/javaee:tlds/javaee:config/javaee:doc-title"/></h2>
          </center>
          <table BORDER="1" CELLPADDING="3" CELLSPACING="0" WIDTH="100%">
            <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
              <td COLSPAN="2"><font SIZE="+2">
                <b>Tag Libraries</b>
              </font></td>
            </tr>
            <xsl:apply-templates select="/javaee:tlds/javaee:taglib"/>
          </table>
          <p/>
          <hr/>
          <!-- =========== START OF NAVBAR =========== -->
          <a name="navbar_bottom"><!-- --></a>
          <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_bottom_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev"> <font CLASS="NavBarFont1Rev"><b>&#160;Overview&#160;</b></font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Library&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a HREF="help-doc.html"><font CLASS="NavBarFont1"><b>Help</b></font></a>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                <!--&#160;PREV&#160;-->
                <!--&#160;NEXT&#160;-->
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="overview-summary.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
          </table>
          <!-- =========== END OF NAVBAR =========== -->
          <hr/>
          <small><i>
          Output Generated by 
          <a href="http://taglibrarydoc.dev.java.net/" target="_blank">Tag Library Documentation Generator</a>.
          Java, JSP, and JavaServer Pages are trademarks or 
          registered trademarks of Sun Microsystems, Inc. in the US and other
          countries.  Copyright 2002-4 Sun Microsystems, Inc.
          4150 Network Circle
          Santa Clara, CA 95054, U.S.A.
          All Rights Reserved. 
          </i></small>
        </body>
      </html>
    </xsl:template>
    
    <xsl:template match="javaee:taglib">
      <tr BGCOLOR="white" valign="top" CLASS="TableRowColor">
        <td WIDTH="20%"><b>
          <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="javaee:short-name"/>/tld-summary.html</xsl:attribute>
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
        </b></td>
        <td>
          <xsl:choose>
              <xsl:when test="javaee:description!=''">
                <pre>
                  <xsl:value-of select="javaee:description" disable-output-escaping="yes"/>
                </pre>
              </xsl:when>
              <xsl:otherwise>
                <i>No Description</i>
              </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </xsl:template>
</xsl:stylesheet> 
