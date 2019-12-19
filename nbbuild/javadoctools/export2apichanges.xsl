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
    <xsl:import href="jsonhelp.xsl" />
    <xsl:import href="export2allmodules.xsl" />
    <xsl:output method="html"/>
    <xsl:param name="date"  />
    <xsl:param name="changes-since-year"  />
    <xsl:param name="changes-since-month"  />
    <xsl:param name="changes-since-day"  />
    <xsl:param name="include-introduction" select="'true'" />
    <xsl:param name="url-prefix" select="''" />
    <xsl:param name="maturity" />
    <xsl:param name="version" />
    <xsl:param name="releaseinfo" />
    <xsl:param name="allmodule" />
     
    <xsl:template match="/" >
        <xsl:choose>
            <xsl:when test="$include-introduction='true'" >
                <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
                <html>
                    <xsl:call-template name="htmlheader" >
                        <xsl:with-param name="title" >APIs Changes</xsl:with-param>
                        <xsl:with-param name="maturity" select="$maturity" />
                        <xsl:with-param name="version" select="$version"/>
                    </xsl:call-template>
                    <body>
                        <xsl:call-template name="htmlmainmenu" >
                            <xsl:with-param name="title" >APIs Changes</xsl:with-param>
                            <xsl:with-param name="maturity" select="$maturity" />
                            <xsl:with-param name="version" select="$version"/> 
                            <xsl:with-param name="releaseinfo" select="$releaseinfo"/>
                            <xsl:with-param name="menukey" >apichanges</xsl:with-param>
                        </xsl:call-template>
                        
            
                        <div class="apidocmaincontent">
                            <xsl:call-template name="build-docmenu" >
                                <xsl:with-param name="menukey" >apichanges</xsl:with-param>
                                <xsl:with-param name="date" select="$date"/>
                            </xsl:call-template>
                
                            <div class="innercontent">
                                <div class="abstract">
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
                                    <xsl:text>). </xsl:text>
                                </div>
                                <hr/>
                                    <!--
                                    There are also other documents that list changes 
                                    made for releaso of the Oracle era: </xsl:text>
                                <p>
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
                                    <a href="http://bits.netbeans.org/8.2/javadoc/apichanges.html">release 8.2</a>.
                                </p>
                                <p>Apache era:</p>
                                <xsl:variable name="currentversion" select="document($releaseinfo)/*/@position"/>
                                <xsl:for-each select="document($releaseinfo)//release">
                                    <xsl:sort data-type="number" select="@position" order="ascending" />
                                    <xsl:choose>
                                        <xsl:when test="$currentversion = @position">,current release</xsl:when>
                                        <xsl:otherwise>
                                            <xsl:element name="a">
                                                <xsl:attribute name="href">
                                                    <xsl:value-of select="@apidocurl"/>/apichanges.html</xsl:attribute>
                                                <xsl:attribute name="target">_top</xsl:attribute>,release <xsl:value-of select="@version"/>      
                                            </xsl:element>
                                        </xsl:otherwise>
                                    </xsl:choose>
                    
                                </xsl:for-each>
                                .
                               <a href="http://bits.netbeans.org/9.0/javadoc/apichanges.html">release 9.0</a>,
                                <a href="http://bits.netbeans.org/10.0/javadoc/apichanges.html">release 10.0</a>,
                                <a href="http://bits.netbeans.org/11.0/javadoc/apichanges.html">release 11.0</a>.-->
                                <xsl:call-template name="do-the-table" />
                            </div>
                        </div>
                        <div class="apidocleft">
                            <div class="apidocleft">
                                <xsl:call-template name="listallmodules" />
                            </div>
                            <!--<xsl:call-template name="listallmodules" />-->
                            <!--<xsl:template match="document($allmodule)">-->
                            <!--<xsl:apply-templates select="document($allmodule)" />-->
                            <!--</xsl:template>-->
                        </div>
                        <xsl:call-template name="htmlfooter" />
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
                    <xsl:comment>
                        <xsl:value-of select="$important.change"/>
                    </xsl:comment>
                    <xsl:call-template name="change-url">
                        <xsl:with-param name="span">
                            <xsl:value-of select="$important.change"/>
                        </xsl:with-param>
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
        <span>
            <xsl:attribute name="style">
                <xsl:value-of select="$span"/>
            </xsl:attribute>
            <xsl:value-of select="substring-before(@url,'/')"/>
        </span>:
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="$url-prefix"/>
                <xsl:value-of select="@url"/>#<xsl:value-of select="@id"/>
            </xsl:attribute>
            <xsl:apply-templates select="summary/node()"/>
        </a>
    </xsl:template>
        
    
</xsl:stylesheet>


