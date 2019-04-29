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
    <xsl:import href="apichanges.xsl" />

    <xsl:output method="html"/>
    <xsl:param name="date"  />
    <xsl:param name="changes-since-year"  />
    <xsl:param name="changes-since-month"  />
    <xsl:param name="changes-since-day"  />
    <xsl:param name="include-introduction" select="'true'" />
    <xsl:param name="url-prefix" select="''" />

    <xsl:template match="/" >
      <xsl:choose>
        <xsl:when test="$include-introduction='true'" >
            <html>
            <head>
                <!-- projects.netbeans.org -->
               <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
               <title>Apache NetBeans (incubating) API Changes since Last Release</title>
                <link rel="stylesheet" href="netbeans.css" type="text/css"/>

              <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
              <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />
              <link type="application/atom+xml" rel="alternate" href="apichanges.atom"/>

            </head>

            <body>


            <center>
                <h1>Apache NetBeans (incubating) API Changes since Last Release</h1>
                <h3>Current Development Version</h3>
                <xsl:if test="$date" >
                    <xsl:value-of select="$date" />
                    <p/>
                </xsl:if>
            </center>

            This document highlights changes in <a href="index.html">NetBeans APIs</a> 
            since previous version (i.e.
                <xsl:value-of select="$changes-since-day" />
                <xsl:text> </xsl:text>
                <xsl:choose>
                    <xsl:when test="$changes-since-month=1">Jan</xsl:when>
                    <xsl:when test="$changes-since-month=2">Feb</xsl:when>
                    <xsl:when test="$changes-since-month=3">Mar</xsl:when>
                    <xsl:when test="$changes-since-month=4">Apr</xsl:when>
                    <xsl:when test="$changes-since-month=5">May</xsl:when>
                    <xsl:when test="$changes-since-month=6">Jun</xsl:when>
                    <xsl:when test="$changes-since-month=7">Jul</xsl:when>
                    <xsl:when test="$changes-since-month=8">Aug</xsl:when>
                    <xsl:when test="$changes-since-month=9">Sep</xsl:when>
                    <xsl:when test="$changes-since-month=10">Oct</xsl:when>
                    <xsl:when test="$changes-since-month=11">Nov</xsl:when>
                    <xsl:when test="$changes-since-month=12">Dec</xsl:when>
                </xsl:choose> 
                <xsl:text> </xsl:text>
                <xsl:value-of select="$changes-since-year" /> 
                <xsl:text>). There are also other documents that list changes 
                made for </xsl:text>
                <a href="http://www.netbeans.org/download/5_0/javadoc/apichanges.html">release 5.0</a>, 
                <a href="http://www.netbeans.org/download/5_5/javadoc/apichanges.html">release 5.5</a>,
                <a href="http://bits.netbeans.org/6.0/javadoc/apichanges.html">release 6.0</a>,
                <a href="http://bits.netbeans.org/6.1/javadoc/apichanges.html">release 6.1</a>,
                <a href="http://bits.netbeans.org/6.5/javadoc/apichanges.html">release 6.5</a>,
                <a href="http://bits.netbeans.org/6.7/javadoc/apichanges.html">release 6.7</a>,
                <a href="http://bits.netbeans.org/6.8/javadoc/apichanges.html">release 6.8</a>,
                <a href="http://bits.netbeans.org/6.9/javadoc/apichanges.html">release 6.9</a>,
                <a href="http://bits.netbeans.org/6.9.1/javadoc/apichanges.html">release 6.9.1</a>,
                <a href="http://bits.netbeans.org/7.0/javadoc/apichanges.html">release 7.0</a>,
                <a href="http://bits.netbeans.org/7.0.1/javadoc/apichanges.html">release 7.0.1</a>,
                <a href="http://bits.netbeans.org/7.1/javadoc/apichanges.html">release 7.1</a>,
                <a href="http://bits.netbeans.org/7.1.1/javadoc/apichanges.html">release 7.1.1</a>,
                <a href="http://bits.netbeans.org/7.1.2/javadoc/apichanges.html">release 7.1.2</a>,
                <a href="http://bits.netbeans.org/7.2/javadoc/apichanges.html">release 7.2</a>,
                <a href="http://bits.netbeans.org/7.2.1/javadoc/apichanges.html">release 7.2.1</a>,
                <a href="http://bits.netbeans.org/7.3/javadoc/apichanges.html">release 7.3</a>,
                <a href="http://bits.netbeans.org/7.3.1/javadoc/apichanges.html">release 7.3.1</a>,
                <a href="http://bits.netbeans.org/7.4/javadoc/apichanges.html">release 7.4</a>,
                <a href="http://bits.netbeans.org/8.0/javadoc/apichanges.html">release 8.0</a>,
                <a href="http://bits.netbeans.org/8.0.1/javadoc/apichanges.html">release 8.0.1</a>,
                <a href="http://bits.netbeans.org/8.1/javadoc/apichanges.html">release 8.1</a>,
                <a href="http://bits.netbeans.org/8.2/javadoc/apichanges.html">release 8.2</a>,
                <a href="http://bits.netbeans.org/9.0/javadoc/apichanges.html">release 9.0</a>,
                <a href="http://bits.netbeans.org/10.0/javadoc/apichanges.html">release 10.0</a>,
                <a href="http://bits.netbeans.org/11.0/javadoc/apichanges.html">release 11.0</a>.
            <xsl:call-template name="do-the-table" />
            </body>
            </html>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="do-the-table" />
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:template>
    
    <xsl:template name="do-the-table" >
        <ul>
            <xsl:apply-templates select="//change" mode="global-overview">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="change" mode="global-overview">
        <li>
            <xsl:variable name="important.change">
                <xsl:choose>
                    <xsl:when test="compatibility/@binary='incompatible'">color: #ff0000; font-weight: bold</xsl:when>
                    <xsl:when test="compatibility/@deletion='yes'">font-weight: bold</xsl:when>
                    <xsl:when test="compatibility/@source='incompatible'">color: #3f0000; font-weight: bold</xsl:when>
                    <xsl:otherwise>none</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not ($important.change = 'none')">
                    <xsl:comment><xsl:value-of select="$important.change"/></xsl:comment>
                    <xsl:call-template name="change-url">
                        <xsl:with-param name="span"><xsl:value-of select="$important.change"/></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="change-url"/>
                </xsl:otherwise>
            </xsl:choose> 
        </li>
    </xsl:template>
    
    <xsl:template name="change-url" mode="global-overview">
        <xsl:param name="span"></xsl:param>
        <xsl:if test="date">(<xsl:apply-templates select="date"/>)<xsl:text> </xsl:text></xsl:if>
        <span><xsl:attribute name="style"><xsl:value-of select="$span"/></xsl:attribute>
        <xsl:value-of select="substring-before(@url,'/')"/></span>:
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$url-prefix"/><xsl:value-of select="@url"/>#<xsl:value-of select="@id"/></xsl:attribute>
            <xsl:apply-templates select="summary/node()"/>
        </a>
    </xsl:template>
        
    
</xsl:stylesheet>


