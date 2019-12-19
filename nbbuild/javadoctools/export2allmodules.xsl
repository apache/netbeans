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
    <xsl:import href="jsonhelp.xsl" />
    <xsl:output method="html"/>
    <xsl:param name="maturity" />
    <xsl:template name="listallmodules" >
        <input type="text" id="searchinput" onkeyup="searchFunction()" placeholder="Search for class..."/>
        <ul class="moduleslist">
            <xsl:for-each select="//module[not (@name = '_no module_')]" >
                <xsl:sort order="ascending" select="@name" />
                <li class="module">
                    <xsl:call-template name="modulewithclass" />
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>
    <xsl:template name="modulewithclass">
        <xsl:attribute name="style">
            <xsl:choose>
                <xsl:when test="descendant::api[@category='stable' and @group='java']">background-color:#ffffff</xsl:when>
                <xsl:when test="descendant::api[@category='official' and @group='java']">background-color:#ffffff</xsl:when>
                <xsl:when test="descendant::api[@category='devel' and @group='java']">background-color:#ddcc80</xsl:when>
                <xsl:when test="descendant::api[@category='deprecated' and @group='java']">text-decoration: line-through</xsl:when>
                <xsl:otherwise>background-color:#e0c0c0</xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
        <span class="modules">
            <xsl:value-of select="@name" />
        </span>
        (<a><xsl:attribute name="href"><xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>javadoc</a>)
        <ul class="modulesclasslist">
            <xsl:variable name="modulename" select="substring-before(@target,'/')" />
            <xsl:for-each select="//class[($modulename = substring-before(@url,'/'))]" >
                <xsl:sort order="ascending" select="@name" />
                <xsl:element name="li">
                    <xsl:attribute name="class">
                        <xsl:if test="@interface='false'">class</xsl:if>
                        <xsl:if test="@interface='true'">interface</xsl:if>
                    </xsl:attribute>
                    <xsl:call-template name="class" />
                </xsl:element>
            </xsl:for-each>
        </ul>
    </xsl:template>
    <xsl:template name="class">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="@url" /></xsl:attribute>
            <xsl:value-of select="@name" />
        </a>
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
                <xsl:attribute name="href">
                    <xsl:value-of select="substring-before(@target,'/')" />/allclasses-frame.html</xsl:attribute>
                <xsl:attribute name="target">packageFrame</xsl:attribute>
                <xsl:value-of select="@name" />
            </a>
            (<a>
                <xsl:attribute name="href">
                    <xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>
                <xsl:attribute name="target">classFrame</xsl:attribute>javadoc</a>)
        </span>
        <br/>
    </xsl:template>
</xsl:stylesheet>


