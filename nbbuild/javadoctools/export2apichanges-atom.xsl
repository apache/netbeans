<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:xalan="http://xml.apache.org/xslt" xmlns:xhtml="http://www.w3.org/1999/xhtml">
    <xsl:import href="apichanges.xsl" />
    <xsl:import href="jsonhelp.xsl" />
    <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
    <xsl:param name="date"/>
    <xsl:param name="url-prefix" select="''"/>
    <xsl:param name="maturity" />
    <xsl:template match="/" >
        <atom:feed>
            <atom:id>urn:netbeans-org:apichanges</atom:id>
            <xsl:element name="atom:title">
                <xsl:call-template name="apachenetbeanstext" >
                    <xsl:with-param name="maturity">
                        <xsl:value-of select="$maturity"/>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:text>API Changes</xsl:text>
            </xsl:element>
            <atom:author>
                <atom:name>netbeans.org</atom:name>
            </atom:author>
            <atom:link rel="alternate" type="text/html" href="apichanges.html"/>
            <atom:link rel="self" type="application/xml+atom" href="http://deadlock.netbeans.org/job/nbms-and-javadoc/javadoc/apichanges.atom"/>
            <atom:updated>
                <xsl:value-of select="$date"/>
            </atom:updated>
            <xsl:apply-templates select="//change">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </atom:feed>
    </xsl:template>

    <xsl:template match="change">
        <atom:entry>
            <xsl:if test="@id">
                <atom:id>urn:netbeans-org:apichanges:<xsl:value-of select="@id"/></atom:id>
            </xsl:if>
            <atom:title type="xhtml">
                <xhtml:div>[<xsl:value-of select="translate(substring-before(@url,'/'), '-', '.')"/>] <xsl:apply-templates select="summary/node()" mode="xhtmlify"/></xhtml:div>
            </atom:title>
            <!-- XXX is the relative URL legal? -->
            <atom:link rel="alternate" type="text/html">
                <xsl:attribute name="href">
                    <xsl:value-of select="$url-prefix"/>
                    <xsl:value-of select="@url"/>#<xsl:value-of select="@id"/>
                </xsl:attribute>
            </atom:link>
            <xsl:if test="date">
                <atom:updated>
                    <xsl:value-of select="date/@year"/>-<xsl:if test="string-length(date/@month) = 1">0</xsl:if>
                    <xsl:value-of select="date/@month"/>-<xsl:if test="string-length(date/@day) = 1">0</xsl:if>
                    <xsl:value-of select="date/@day"/>T00:00:00Z</atom:updated>
            </xsl:if>
            <xsl:if test="author">
                <atom:author>
                    <atom:name>
                        <xsl:value-of select="author/@login"/>
                    </atom:name>
                    <atom:email>
                        <xsl:value-of select="author/@login"/>@netbeans.org</atom:email>
                </atom:author>
            </xsl:if>
            <atom:summary type="xhtml">
                <xhtml:div>
                    <xsl:apply-templates select="description" mode="xhtmlify"/>
                </xhtml:div>
            </atom:summary>
        </atom:entry>
    </xsl:template>

    <xsl:template match="*" mode="xhtmlify" priority="2">
        <xsl:element name="{local-name(.)}" namespace="http://www.w3.org/1999/xhtml">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|node()" mode="xhtmlify" priority="1">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
